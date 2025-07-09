package me.kiriyaga.essentials.feature.module.impl.misc;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;
import me.kiriyaga.essentials.setting.impl.IntSetting;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class UnfocusedCpuModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("limit", 15, 5, 30));

    public UnfocusedCpuModule() {
        super("unfocused cpu", "Limits your cpu/frame generate usage while unfocused", Category.misc, "unfocusedcpu");
    }
}
