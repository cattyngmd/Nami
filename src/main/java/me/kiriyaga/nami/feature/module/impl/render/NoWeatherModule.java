
package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import net.minecraft.client.option.Perspective;

public class NoWeatherModule extends Module {
    public float cameraYaw;
    public float cameraPitch;

    private Perspective previousPerspective;

    public NoWeatherModule() {
        super("no weather", "Disables rendering of weather.", Category.visuals, "noweather", "nowether", "nowather", "тщцуферук");
    }
}
