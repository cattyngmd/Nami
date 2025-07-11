package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class AutoFovModule extends Module {

    public final IntSetting fov = addSetting(new IntSetting("fov", 110, 1, 162));

    public AutoFovModule() {
        super("auto fov", "Set up your custom fov.", Category.misc, "autofov", "fov", "atofov", "фгещащч");
    }

    @Override
    public void onEnable() {
        if (MINECRAFT != null && MINECRAFT.options != null && MINECRAFT.options.getFov() != null) {
            ((ISimpleOption) (Object) MINECRAFT.options.getFov()).setValue(fov.get());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onTick(PostTickEvent ev) {
        if (MINECRAFT == null || MINECRAFT.options == null || MINECRAFT.options.getFov() == null) return;

        int d = MINECRAFT.options.getFov().getValue();

        if (d != fov.get()) {
            ((ISimpleOption) (Object) MINECRAFT.options.getFov()).setValue(fov.get());
        }
    }
}
