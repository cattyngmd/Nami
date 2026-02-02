package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import net.minecraft.client.CameraType;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class FreeLookFeature extends Feature { // todo this shit broke
    public float cameraYaw;
    public float cameraPitch;

    private CameraType previousPerspective;

    public DoubleSetting sensivity = addSetting(new DoubleSetting("Sensivity", 5, 2, 15));


    public FreeLookFeature() {
        super("FreeLook", "Look around freely without moving your real yaw/pitch.", FeatureCategory.of("Render"), "freelook", "freelok", "third");
    }

    @Override
    public void onEnable() {
        if (MC.player == null || MC.level == null) {
            toggle();
            return;
        }

        cameraYaw = MC.player.getYRot();
        cameraPitch = MC.player.getXRot();

        previousPerspective = MC.options.getCameraType();
        if (previousPerspective != CameraType.THIRD_PERSON_BACK) {
            MC.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDisable() {
        if (previousPerspective != null && MC.options.getCameraType() != previousPerspective) {
            MC.options.setCameraType(previousPerspective);
        }
        if (previousPerspective == null) {
            MC.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }
}
