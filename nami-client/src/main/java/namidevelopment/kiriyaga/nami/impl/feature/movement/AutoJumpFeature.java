package namidevelopment.kiriyaga.nami.impl.feature.movement;

import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.nami.mixin.DuckKeyMapping;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import static namidevelopment.kiriyaga.api.NamiApi.MC;

@RegisterFeature
public class AutoJumpFeature extends Feature {

    public AutoJumpFeature() {
        super("AutoJump", "Automatically makes you jump.", FeatureCategory.of("Movement"),"autojump");
    }

    @Override
    public void onDisable() {
        setJumpHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
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
