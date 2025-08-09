package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.network.ClientPlayerInteractionManager;
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
        super("auto click", "Automatically clicks.", ModuleCategory.of("combat"), "autoclick", "фгещсдшсл");
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

    private void performLeftClick() {
        ClientPlayerInteractionManager im = MC.interactionManager;
        if (im != null) {
            MC.player.swingHand(Hand.MAIN_HAND);
            im.attackEntity(MC.player, MC.targetedEntity != null ? MC.targetedEntity : null);
        }
    }

    private void performRightClick() {
        MC.player.swingHand(Hand.MAIN_HAND);
        MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
    }
}
