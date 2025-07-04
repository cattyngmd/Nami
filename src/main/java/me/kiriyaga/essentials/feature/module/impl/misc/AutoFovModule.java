package me.kiriyaga.essentials.feature.module.impl.misc;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;
import me.kiriyaga.essentials.setting.impl.IntSetting;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AutoFovModule extends Module {

    public final IntSetting fov = addSetting(new IntSetting("fov", 110, 1, 162));

    public AutoFovModule() {
        super("auto fov", "Sets up fov on join.", Category.misc, "autofov", "fov", "atofov", "фгещащч");
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
