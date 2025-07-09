package me.kiriyaga.essentials.feature.module.impl.misc;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AutoGammaModule extends Module {

    public final DoubleSetting gamma = addSetting(new DoubleSetting("gamma", 2, 1, 25));

    public AutoGammaModule() {
        super("auto gamma", "Set up you custom gamma", Category.misc, "autogamma", "gamma", "autogmam", "фгещпфььф");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onTick(PostTickEvent ev){
        double d = MINECRAFT.options.getGamma().getValue();

        if (d != gamma.get())
            ((ISimpleOption) (Object) MINECRAFT.options.getGamma()).setValue(gamma.get());
    }
}
