package me.kiriyaga.nami.impl.feature.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import net.minecraft.client.CameraType;

import static me.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class NoWeatherFeature extends Feature {
    public float cameraYaw;
    public float cameraPitch;

    private CameraType previousPerspective; // todo this shit broke

    public NoWeatherFeature() {
        super("NoWeather", "Disables rendering of weather.", FeatureCategory.of("Render"), "noweather", "nowether", "nowather");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onUpdate (PreTickEvent ev){
        if (MC == null || MC.level == null || MC.player == null)
            return;
        this.clearDisplayInfo();

        String weather;

        if (MC.level.isRaining()) {
            if (MC.level.isThundering()) {
                weather = "thunder";
            } else {
                weather = "rain";
            }
        } else {
            weather = "clear";
        }

        this.addDisplayInfo(weather);
    }
}
