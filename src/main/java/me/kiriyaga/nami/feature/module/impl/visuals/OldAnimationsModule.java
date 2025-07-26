package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.network.ClientPlayerEntity;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class OldAnimationsModule extends Module {

    public OldAnimationsModule() {
        super("old animations", "Makes your hands animated like 1.8.", ModuleCategory.of("visuals"), "oldanimations","щдвфтшьфешщты");
    }
}