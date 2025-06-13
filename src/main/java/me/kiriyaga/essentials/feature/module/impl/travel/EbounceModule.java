package me.kiriyaga.essentials.feature.module.impl.travel;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;

import static me.kiriyaga.essentials.Essentials.*;

public class EbounceModule extends Module {

    private boolean startSprinting;

    public EbounceModule() {
        super("Ebounce", "Simple ebounce (actually very simple)", Category.TRAVEL, "ebnce", "уищгтсу");
    }

    @Override
    public void onEnable() {
        if (MINECRAFT.player == null || MINECRAFT.player.getAbilities().allowFlying) return;
        if (MINECRAFT.player.getPos().multiply(1, 0, 1).length() < 100) return;

        startSprinting = MINECRAFT.player.isSprinting();
    }

    @Override
    public void onDisable() {
        if (MINECRAFT.player == null) return;

        MINECRAFT.player.setSprinting(startSprinting);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof PlayerSpawnPositionS2CPacket) {
            onEnable();
        }
    }

    @SubscribeEvent
    public void OnPreTick(PreTickEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.player.getAbilities().allowFlying) return;

        MINECRAFT.player.setSprinting(true);

        handleBounceMovement();


            MINECRAFT.player.networkHandler.sendPacket(new ClientCommandC2SPacket(
                    MINECRAFT.player,
                    ClientCommandC2SPacket.Mode.START_FALL_FLYING
            ));
    }

    private void handleBounceMovement() {
            if (MINECRAFT.player.isOnGround())
                MINECRAFT.player.jump();
    }
}