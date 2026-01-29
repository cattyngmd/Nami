package me.kiriyaga.nami.impl.feature.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.mixin.DuckKeyMapping;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.SERVER_SERVICE;

@RegisterFeature
public class AutoWalkFeature extends Feature {

    private final BoolSetting setbackStop = addSetting(new BoolSetting("SetbackStop", true));

    public AutoWalkFeature() {
        super("AutoWalk", "Automatically makes you walk.", FeatureCategory.of("Movement"),"autowalk");
    }

    @Override
    public void onDisable() {
        if (MC.player == null || MC.level == null)
            return;

        setWalkHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null)
            return;

        if (setbackStop.get() && !SERVER_SERVICE.hasElapsedSinceSetback(5000))
            return;

        setWalkHeld(true);
    }

    private void setWalkHeld(boolean held) {
        KeyMapping walkKey = MC.options.keyUp;
        InputConstants.Key boundKey = ((DuckKeyMapping) walkKey).getKey();
        int keyCode = boundKey.getValue();
        boolean physicallyPressed = InputConstants.isKeyDown(MC.getWindow(), keyCode);
        walkKey.setDown(physicallyPressed || held);
    }
}
