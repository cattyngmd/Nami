package namidevelopment.kiriyaga.nami.impl.feature.impl.visuals;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import net.minecraft.client.CameraType;

import static namidevelopment.kiriyaga.nami.Nami.MC;

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
