package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;
import me.kiriyaga.essentials.setting.impl.IntSetting;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AutoFovModule extends Module {

    public final IntSetting fov = addSetting(new IntSetting("Gamma", 110, 1, 162));

    public AutoFovModule() {
        super("Auto Fov", "Sets up fov on join.", Category.WORLD, "autofov", "fov", "atofov", "фгещащч");
    }

    @Override
    public void onEnable(){
        ((ISimpleOption) (Object) MINECRAFT.options.getFov()).setValue(fov.get());
    }
}
