package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.mixininterface.IPlayerInteractEntityC2SPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class CriticalsModule extends Module {

    public enum CritMode { PACKET }

    private final EnumSetting<CritMode> mode = addSetting(new EnumSetting<>("Mode", CritMode.PACKET));

    public CriticalsModule() {
        super("Criticals", "Changes player movement for always critting.", ModuleCategory.of("Combat"));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void PacketSendEvent(PacketSendEvent event) {
        if (!(event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet)) return;
        if (packet.getType() != ServerboundInteractPacket.ActionType.ATTACK) return;

        if (!isValidAttackContext()) return;

        Entity target = packet.getEntity();
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;

        if (MC.player.isHandsBusy()) {
            handleRidingAttack(target);
            return;
        }

        handleGroundedAttack();
    }

    private boolean isValidAttackContext() {
        return MC.player != null && MC.level != null && !MC.player.isHandsBusy() && !MC.player.isFallFlying() && !MC.player.isInWater() && !MC.player.isInLava() && !MC.player.isSuppressingSlidingDownLadder() && !MC.player.hasEffect(MobEffects.BLINDNESS);
    }

    private void handleRidingAttack(Entity target) {
        if (mode.get() == CritMode.PACKET) {
            for (int i = 0; i < 5; i++) {
                MC.getConnection().send(ServerboundInteractPacket.createAttackPacket(target, MC.player.isShiftKeyDown()));
                MC.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }

    private void handleGroundedAttack() {
        spoofCritical();
    }

    private void spoofCritical() {
        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        switch (mode.get()) {
            case PACKET -> spoofPacketCrit(x, y, z);
        }
    }

    private void spoofPacketCrit(double x, double y, double z) {
        if (!MC.player.onGround()) return;

        MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
        MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
    }
}