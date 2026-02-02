package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.KeyBindSetting;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AirJumpFeature extends Feature {

    public final KeyBindSetting useKey = addSetting(new KeyBindSetting("Use", KeyBindSetting.KEY_NONE));
    public final BoolSetting setOnGround = addSetting(new BoolSetting("SetOnGround", true));

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
