package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoKeyModule extends Module {

    private KeyBinding[] trackedKeys;


    private final Map<KeyBinding, Boolean> savedKeyStates = new HashMap<>();

    public AutoKeyModule() {
        super("AutoKey", "Holds all physically pressed keys automatically.", ModuleCategory.of("Movement"),"autokey");
    }

    @Override
    public void onEnable() {
        trackedKeys = new KeyBinding[]{
                MC.options.forwardKey,
                MC.options.backKey,
                MC.options.leftKey,
                MC.options.rightKey,
                MC.options.jumpKey,
                MC.options.sprintKey,
                MC.options.attackKey,
                MC.options.sneakKey,
                MC.options.useKey
        };

        savedKeyStates.clear();
        for (KeyBinding key : trackedKeys) {
            InputUtil.Key boundKey = ((KeyBindingAccessor) key).getBoundKey();
            int keyCode = boundKey.getCode();
            boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
            if (physicallyPressed) {
                savedKeyStates.put(key, true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (trackedKeys == null) return;
        for (KeyBinding key : savedKeyStates.keySet()) {
            key.setPressed(false);
        }
        savedKeyStates.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        if (trackedKeys == null) return;
        for (KeyBinding key : trackedKeys) {
            if (savedKeyStates.getOrDefault(key, false)) {
                key.setPressed(true);
            } else {
                key.setPressed(false);
            }
        }
    }
}