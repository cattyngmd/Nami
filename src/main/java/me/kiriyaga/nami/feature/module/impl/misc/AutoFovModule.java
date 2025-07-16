package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;

public class AutoFovModule extends Module {

    public final IntSetting fov = addSetting(new IntSetting("fov", 110, 1, 162));

    public AutoFovModule() {
        super("auto fov", "Set up your custom fov.", Category.misc, "autofov", "fov", "atofov", "фгещащч");
    }

    @Override
    public void onEnable() {
        if (MC != null && MC.options != null && MC.options.getFov() != null) {
            ((ISimpleOption) (Object) MC.options.getFov()).setValue(fov.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onTick(PostTickEvent ev) {
        if (MC == null || MC.options == null || MC.options.getFov() == null) return;

        int d = MC.options.getFov().getValue();

        if (d != fov.get()) {
            ((ISimpleOption) (Object) MC.options.getFov()).setValue(fov.get());
        }
    }
}
