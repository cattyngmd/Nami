package me.kiriyaga.nami.impl.feature.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.mixin.DuckKeyMapping;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import static me.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class AutoJumpFeature extends Feature {

    public AutoJumpFeature() {
        super("AutoJump", "Automatically makes you jump.", FeatureCategory.of("Movement"),"autojump");
    }

    @Override
    public void onDisable() {
        setJumpHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        setJumpHeld(true);
    }

    private void setJumpHeld(boolean held) {
        KeyMapping jumpKey = MC.options.keyJump;
        InputConstants.Key boundKey = ((DuckKeyMapping) jumpKey).getKey();
        int keyCode = boundKey.getValue();
        boolean physicallyPressed = InputConstants.isKeyDown(MC.getWindow(), keyCode);
        jumpKey.setDown(physicallyPressed || held);
    }
}
