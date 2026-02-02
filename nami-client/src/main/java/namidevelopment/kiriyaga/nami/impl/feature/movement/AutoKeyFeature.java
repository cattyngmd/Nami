package namidevelopment.kiriyaga.nami.impl.feature.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.mixin.DuckKeyMapping;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.HashMap;
import java.util.Map;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class AutoKeyFeature extends Feature {

    private KeyMapping[] trackedKeys;


    private final Map<KeyMapping, Boolean> savedKeyStates = new HashMap<>();

    public AutoKeyFeature() {
        super("AutoKey", "Holds all physically pressed keys automatically.", FeatureCategory.of("Movement"),"autokey");
    }

    @Override
    public void onEnable() {
        trackedKeys = new KeyMapping[]{
                MC.options.keyUp,
                MC.options.keyDown,
                MC.options.keyLeft,
                MC.options.keyRight,
                MC.options.keyJump,
                MC.options.keySprint,
                MC.options.keyAttack,
                MC.options.keyShift,
                MC.options.keyUse
        };

        savedKeyStates.clear();
        for (KeyMapping key : trackedKeys) {
            InputConstants.Key boundKey = ((DuckKeyMapping) key).getKey();
            int keyCode = boundKey.getValue();
            boolean physicallyPressed = InputConstants.isKeyDown(MC.getWindow(), keyCode);
            if (physicallyPressed) {
                savedKeyStates.put(key, true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (trackedKeys == null) return;
        for (KeyMapping key : savedKeyStates.keySet()) {
            key.setDown(false);
        }
        savedKeyStates.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        if (trackedKeys == null) return;
        for (KeyMapping key : trackedKeys) {
            if (savedKeyStates.getOrDefault(key, false)) {
                key.setDown(true);
            } else {
                key.setDown(false);
            }
        }
    }
}