package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Box;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class ParkourModule extends Module {

    public ParkourModule() {
        super("parkour", "Automatically jumps at the edge of blocks.", Category.movement, "зфклщгк");
    }

    @Override
    public void onDisable() {
        setJumpHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null) return;

        boolean shouldJump =
                MINECRAFT.player.isOnGround()
                        && !MINECRAFT.player.isSneaking()
                        && MINECRAFT.world.isSpaceEmpty(MINECRAFT.player,
                        MINECRAFT.player.getBoundingBox()
                                .offset(0.0, -0.5, 0.0)
                                .expand(-0.001, 0.0, -0.001));

        setJumpHeld(shouldJump);
    }

    private void setJumpHeld(boolean held) {
        KeyBinding jumpKey = MINECRAFT.options.jumpKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) jumpKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MINECRAFT.getWindow().getHandle(), keyCode);
        jumpKey.setPressed(physicallyPressed || held);
    }
}
