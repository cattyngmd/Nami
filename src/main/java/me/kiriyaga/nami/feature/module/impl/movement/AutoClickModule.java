package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Hand;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoClickModule extends Module {

    public enum ClickButton {
        LEFT,
        RIGHT
    }

    private final EnumSetting<ClickButton> button = addSetting(new EnumSetting<>("button", ClickButton.LEFT));
    private final IntSetting delay = addSetting(new IntSetting("delay", 0, 0, 1200));

    private int tickCounter = 0;

    public AutoClickModule() {
        super("auto click", "Automatically clicks.", ModuleCategory.of("movement"), "autoclick", "фгещсдшсл");
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (tickCounter > 0) {
            tickCounter--;
            return;
        }

        switch (button.get()) {
            case LEFT -> performLeftClick();
            case RIGHT -> performRightClick();
        }

        tickCounter = delay.get();
    }

    private void performRightClick() {
        KeyBinding useKey = MC.options.useKey;
        boolean physicallyPressed = useKey.isPressed();

        useKey.setPressed(true);
    }

    private void performLeftClick() {
        KeyBinding attackKey = MC.options.attackKey;
        boolean physicallyPressed = attackKey.isPressed();

        attackKey.setPressed(true);
    }
}
