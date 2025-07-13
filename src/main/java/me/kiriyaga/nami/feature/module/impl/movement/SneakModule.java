package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.LedgeClipEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.client.network.ClientPlayerEntity;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class SneakModule extends Module {

    public enum Mode {
        Always,
        Corners
    }

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.Always));

    public SneakModule() {
        super("sneak", "Automatically makes you sneak.", Category.movement);
    }

    @Override
    public void onDisable() {
        if (MINECRAFT.player != null) {
            MINECRAFT.player.setSneaking(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(PreTickEvent event) {
        ClientPlayerEntity player = MINECRAFT.player;
        if (player == null) return;

        if (mode.get() == Mode.Always) {
            player.setSneaking(true);
        } else if (mode.get() == Mode.Corners) {
            player.setSneaking(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLedgeClip(LedgeClipEvent event) {
        if (mode.get() != Mode.Corners) return;

        assert MINECRAFT.player != null;
        if (!MINECRAFT.player.isSneaking()) {
            MINECRAFT.player.setSneaking(true);
        }
        event.cancel();
        event.setClipped(true);
    }

//    private void setSneakHeld(boolean held) {
//        KeyBinding sneakKey = MINECRAFT.options.sneakKey;
//        InputUtil.Key boundKey = ((KeyBindingAccessor) sneakKey).getBoundKey();
//        int keyCode = boundKey.getCode();
//        boolean physicallyPressed = InputUtil.isKeyPressed(MINECRAFT.getWindow().getHandle(), keyCode);
//        sneakKey.setPressed(physicallyPressed || held);
//    }
}
