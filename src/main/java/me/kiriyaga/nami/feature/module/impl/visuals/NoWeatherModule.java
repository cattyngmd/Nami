
package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.client.option.Perspective;

@RegisterModule(category = "visuals")
public class NoWeatherModule extends Module {
    public float cameraYaw;
    public float cameraPitch;

    private Perspective previousPerspective;

    public NoWeatherModule() {
        super("no weather", "Disables rendering of weather.", ModuleCategory.of("visuals"), "noweather", "nowether", "nowather", "тщцуферук");
    }
}
