package me.kiriyaga.nami.impl.feature.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.KeyBindSetting;

import static me.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AirJumpFeature extends Feature {

    private final KeyBindSetting useKey = addSetting(new KeyBindSetting("Use", KeyBindSetting.KEY_NONE));
    private final BoolSetting setOnGround = addSetting(new BoolSetting("SetOnGround", true));

    public AirJumpFeature() {
        super("AirJump", "Allows jumping in air when pressing key.", FeatureCategory.of("Movement"), "airjump");
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
