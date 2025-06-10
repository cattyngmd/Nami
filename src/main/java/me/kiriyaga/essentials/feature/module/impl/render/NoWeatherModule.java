
package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import net.minecraft.client.option.Perspective;

public class NoWeatherModule extends Module {
    public float cameraYaw;
    public float cameraPitch;

    private Perspective previousPerspective;

    public NoWeatherModule() {
        super("No Weather", "Disables weather.", Category.RENDER, "noweather", "nowether", "nowather", "тщцуферук");
    }
}
