package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

import static me.kiriyaga.nami.Nami.MINECRAFT;

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
