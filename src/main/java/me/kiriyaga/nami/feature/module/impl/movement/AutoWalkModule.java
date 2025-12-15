package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.SERVER_MANAGER;

@RegisterModule
public class AutoWalkModule extends Module {

    private final BoolSetting setbackStop = addSetting(new BoolSetting("SetbackStop", true));

    public AutoWalkModule() {
        super("AutoWalk", "Automatically makes you walk.", ModuleCategory.of("Movement"),"autowalk");
    }

    @Override
    public void onDisable() {
        if (MC.player == null || MC.world == null)
            return;

        setWalkHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null)
            return;

        if (setbackStop.get() && SERVER_MANAGER.hasElapsedSinceSetback(5000))
            return;

        setWalkHeld(true);
    }

    private void setWalkHeld(boolean held) {
        KeyBinding walkKey = MC.options.forwardKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) walkKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow(), keyCode);
        walkKey.setPressed(physicallyPressed || held);
    }
}
