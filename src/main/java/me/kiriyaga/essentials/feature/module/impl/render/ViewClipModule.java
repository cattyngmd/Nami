
package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import net.minecraft.client.option.Perspective;

public class ViewClipModule extends Module {

    public final DoubleSetting distance = addSetting(new DoubleSetting("distance", 3.5, 1, 9));

    public ViewClipModule() {
        super("view clip", "Disables block clipping and extends camera distance.", Category.visuals, "viewclip");
    }
}
