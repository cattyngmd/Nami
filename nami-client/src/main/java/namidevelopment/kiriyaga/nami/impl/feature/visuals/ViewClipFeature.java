package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.Render2DEvent;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import net.minecraft.client.CameraType;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class ViewClipFeature extends Feature {

    public final DoubleSetting distance = addSetting(new DoubleSetting("Distance", 3.5, 1, 9));
    public final BoolSetting animate = addSetting(new BoolSetting("Animation", true));

    private float currentDistance = 3.5f;

    public ViewClipFeature() {
        super("ViewClip", "Disables block clipping and extends camera distance.", FeatureCategory.of("Render"), "viewclip");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender(Render2DEvent ev) {
        CameraType perspective = MC.options.getCameraType();

        if (perspective == CameraType.FIRST_PERSON) {
            currentDistance = 1f;
        } else {
            if (animate.get()) {
                currentDistance += (float) (distance.get() - currentDistance) * 0.12f;
            } else {
                currentDistance = distance.get().floatValue();
            }
        }
    }

    public float getAnimatedDistance() {
        return currentDistance;
    }
}