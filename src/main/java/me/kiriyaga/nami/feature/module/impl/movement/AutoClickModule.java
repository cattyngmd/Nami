package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoClickModule extends Module {

    public enum ClickButton {
        LEFT,
        RIGHT
    }

    private final EnumSetting<ClickButton> button = addSetting(new EnumSetting<>("button", ClickButton.RIGHT));
    private final IntSetting delay = addSetting(new IntSetting("delay", 80, 1, 1200));

    private int tickCounter = 0;
    private boolean clickHeld = false;

    public AutoClickModule() {
        super("auto click", "Automatically clicks like a real mouse press.", ModuleCategory.of("combat"), "autoclick");
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        clickHeld = false;
    }

    @Override
    public void onDisable() {
        releaseKey(getKeyBinding());
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (tickCounter > 0) {
            tickCounter--;
            return;
        }

        if (!clickHeld) {
            setKeyHeld(getKeyBinding(), true);
            clickHeld = true;
            tickCounter = 1;
        } else {
            setKeyHeld(getKeyBinding(), false);
            clickHeld = false;
            tickCounter = delay.get();
        }
    }

    private KeyBinding getKeyBinding() {
        return switch (button.get()) {
            case LEFT -> MC.options.attackKey;
            case RIGHT -> MC.options.useKey;
        };
    }

    private void setKeyHeld(KeyBinding key, boolean held) {
        InputUtil.Key boundKey = ((KeyBindingAccessor) key).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        key.setPressed(physicallyPressed || held);
    }

    private void releaseKey(KeyBinding key) {
        InputUtil.Key boundKey = ((KeyBindingAccessor) key).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        key.setPressed(physicallyPressed);
    }
}