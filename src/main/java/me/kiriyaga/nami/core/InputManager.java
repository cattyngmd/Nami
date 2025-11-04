package me.kiriyaga.nami.core;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.PlayerInput;

import static me.kiriyaga.nami.Nami.*;

// This is mostly for grim, for min-maxing grim checks
// this is the way grim detects our movement input
public class InputManager {

    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean jumping;
    private boolean sneaking;
    private boolean sprinting;
    public void init() {
        EVENT_MANAGER.register(this);
        LOGGER.info("Input Manager loaded");
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST) // should be always first!
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PlayerInputC2SPacket packet) {
            PlayerInput input = packet.comp_3139();

            this.forward = input.comp_3159();// forward
            this.backward = input.comp_3160(); // backward
            this.left = input.comp_3161(); // left
            this.right = input.comp_3162(); // right
            this.jumping = input.comp_3163();// jum
            this.sneaking = input.sneak(); // sneak
            this.sprinting = input.comp_3165(); // sprint

        } else if (event.getPacket() instanceof VehicleMoveC2SPacket veh) {
            // TODO: finish this after i will do deobf for packets
        } else if (event.getPacket() instanceof PlayerMoveC2SPacket move) {

        }
    }

    public boolean hasAnyInput() {
        return forward || backward || left || right || jumping || sneaking || sprinting;
    }

    public boolean isMoving() {
        return forward || backward || left || right;
    }

    public boolean isForward() {
        return forward;
    }

    public boolean isBackward() {
        return backward;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isJumping() {
        return jumping;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public boolean isSprinting() {
        return sprinting;
    }
}
