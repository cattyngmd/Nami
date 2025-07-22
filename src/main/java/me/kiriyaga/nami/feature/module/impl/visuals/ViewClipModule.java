
package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

@RegisterModule
public class ViewClipModule extends Module {

    public final DoubleSetting distance = addSetting(new DoubleSetting("distance", 3.5, 1, 9));

    public ViewClipModule() {
        super("view clip", "Disables block clipping and extends camera distance.", ModuleCategory.of("visuals"), "viewclip");
    }
}
