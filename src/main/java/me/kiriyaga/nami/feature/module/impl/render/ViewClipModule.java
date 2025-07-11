
package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.DoubleSetting;

public class ViewClipModule extends Module {

    public final DoubleSetting distance = addSetting(new DoubleSetting("distance", 3.5, 1, 9));

    public ViewClipModule() {
        super("view clip", "Disables block clipping and extends camera distance.", Category.visuals, "viewclip");
    }
}
