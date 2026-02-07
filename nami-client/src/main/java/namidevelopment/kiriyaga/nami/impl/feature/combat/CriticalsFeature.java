package namidevelopment.kiriyaga.nami.impl.feature.combat;

import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketSendEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;
import namidevelopment.kiriyaga.nami.mixininterface.IPlayerInteractEntityC2SPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicBoolean;

import static namidevelopment.kiriyaga.api.NamiApi.INPUT_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.ROTATION_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;import static namidevelopment.kiriyaga.api.util.entity.PlayerUtils.isPhased;

@RegisterFeature
public class CriticalsFeature extends Feature {

    private Vec3 lastPos = null;

    public enum Mode { PACKET, GRIM}

    public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.PACKET));
    public final BoolSetting onlyPhased = addSetting(new BoolSetting("OnlyPhased", true));
    public final BoolSetting onlyStandingStill = addSetting(new BoolSetting("OnlyStandingStill", true));

    public CriticalsFeature() {
        super("Criticals", "Changes player movement for always critting.", FeatureCategory.of("Combat"));
        onlyPhased.setShowCondition(() -> mode.get() == Mode.GRIM);
        onlyStandingStill.setShowCondition(() -> mode.get() == Mode.GRIM);
    }

    AtomicBoolean b = new AtomicBoolean();

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null)
            return;

        if (lastPos == null)
            lastPos = MC.player.position();

        if (lastPos == MC.player.position())
            b.set(true);
        else
            b.set(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet))
            return;
        if (packet.getType() != ServerboundInteractPacket.ActionType.ATTACK)
            return;

        if (MC.player == null || MC.level == null || MC.player.isHandsBusy() || MC.player.isFallFlying() || MC.player.isInWater() || MC.player.isInLava() || MC.player.isSuppressingSlidingDownLadder() || MC.player.hasEffect(MobEffects.BLINDNESS)) {
            return;
        }

        Entity target = packet.getEntity();

        if (!(target instanceof LivingEntity living) || !living.isAlive() || target instanceof EndCrystal || target instanceof ItemFrame) return;

        if (MC.player.isHandsBusy()) {
            ridingAttack(target);
            return;
        }

        onGroundAttack();
    }

    private void ridingAttack(Entity target) {
        if (mode.get() == Mode.PACKET) {
            for (int i = 0; i < 5; i++) {
                MC.getConnection().send(ServerboundInteractPacket.createAttackPacket(target, MC.player.isShiftKeyDown()));
                MC.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
    }

    private void onGroundAttack() {
        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        switch (mode.get()) {
            case PACKET -> packetCrit(x, y, z);
            case GRIM -> grimCrit(x, y, z);
        }
    }

    private void packetCrit(double x, double y, double z) {
        if (!MC.player.onGround())
            return;

        MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
        MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
    }

    private void grimCrit(double x, double y, double z) {
        if (!MC.player.onGround())
            return;

        if (onlyPhased.get() && !isPhased(MC.player))
            return;

        if (onlyStandingStill.get() && !b.get())
            return;

        float yaw = ROTATION_SERVICE.getStateHandler().getServerYaw();
        float pitch = ROTATION_SERVICE.getStateHandler().getServerPitch();

        MC.getConnection().send(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625, z, yaw, pitch, false, false));
        MC.getConnection().send(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625013579, z, yaw, pitch, false, false));
        MC.getConnection().send(new ServerboundMovePlayerPacket.PosRot(x, y + 1.3579e-6, z, yaw, pitch, false,false));
    }
}