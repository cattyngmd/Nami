package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class AutoWalkModule extends Module {

    public AutoWalkModule() {
        super("auto walk", "Automatically makes you walk.", Category.movement);
    }

    @Override
    public void onDisable() {
        setWalkHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        setWalkHeld(true);
    }

    private void setWalkHeld(boolean held) {
        KeyBinding walkKey = MINECRAFT.options.forwardKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) walkKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MINECRAFT.getWindow().getHandle(), keyCode);
        walkKey.setPressed(physicallyPressed || held);
    }
}
