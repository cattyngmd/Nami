package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;

public class EntityManagerModule extends Module {
    public final IntSetting maxIdleTicks = addSetting(new IntSetting("max idle ticks", 500, 250, 750));
    public EntityManagerModule() {
        super("entity manager", "Allows you to config entity manager settings", Category.CLIENT, "entity", "entitymanager", "enity", "утешеньфтфпук");
    }
}
