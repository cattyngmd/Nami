package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule(category = "misc")
public class AutoGammaModule extends Module {

    public final DoubleSetting gamma = addSetting(new DoubleSetting("gamma", 2, 1, 25));

    public AutoGammaModule() {
        super("auto gamma", "Set up you custom gamma", ModuleCategory.of("misc"), "autogamma", "gamma", "autogmam", "фгещпфььф");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onTick(PostTickEvent ev){
        double d = MC.options.getGamma().getValue();

        if (d != gamma.get())
            ((ISimpleOption) (Object) MC.options.getGamma()).setValue(gamma.get());
    }
}
