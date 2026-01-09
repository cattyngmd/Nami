package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AirJumpModule extends Module {

    private final KeyBindSetting useKey = addSetting(new KeyBindSetting("Use", KeyBindSetting.KEY_NONE));
    private final BoolSetting setOnGround = addSetting(new BoolSetting("SetOnGround", true));

    public AirJumpModule() {
        super("AirJump", "Allows jumping in air when pressing key.", ModuleCategory.of("Movement"), "airjump");
    }

    @Override
    public void onEnable() {
        useKey.setWasPressedLastTick(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onTick(PreTickEvent ev) {
        if (MC.level == null || MC.player == null) return;

        boolean pressed = useKey.isPressed();

        if (pressed && !useKey.wasPressedLastTick()) {
            performAirJump();
        }

        useKey.setWasPressedLastTick(pressed);
    }

    private void performAirJump() {
        if (setOnGround.get())
            MC.player.setOnGround(true);

        MC.player.jumpFromGround();
    }
}
