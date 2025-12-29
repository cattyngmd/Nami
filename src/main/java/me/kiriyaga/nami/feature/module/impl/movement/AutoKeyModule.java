package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.DuckKeyMapping;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.HashMap;
import java.util.Map;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoKeyModule extends Module {

    private KeyMapping[] trackedKeys;


    private final Map<KeyMapping, Boolean> savedKeyStates = new HashMap<>();

    public AutoKeyModule() {
        super("AutoKey", "Holds all physically pressed keys automatically.", ModuleCategory.of("Movement"),"autokey");
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