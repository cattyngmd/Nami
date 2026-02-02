package namidevelopment.kiriyaga.nami.impl.feature.impl.client;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

@RegisterFeature
public class RotationsFeature extends Feature {

    public enum RotationMode {
        MOTION, SILENT
    }

    public final EnumSetting<RotationMode> rotation = addSetting(new EnumSetting<>("Rotation",RotationMode.MOTION));
    public final DoubleSetting rotationSpeed = addSetting(new DoubleSetting("Speed", 360, 25, 360));
    public final DoubleSetting rotationEaseFactor = addSetting(new DoubleSetting("Ease", 1, 0.5, 1));
    public final DoubleSetting rotationThreshold = addSetting(new DoubleSetting("Threshold", 5, 3, 15));
    public final IntSetting ticksBeforeRelease = addSetting(new IntSetting("Hold", 0, 00, 30));
//    public final DoubleSetting jitterAmount = addSetting(new DoubleSetting("jitter amount", 0, 0, 3));
//    public final DoubleSetting jitterSpeed = addSetting(new DoubleSetting("jitter speed", 1, 0.015, 1));
//    public final DoubleSetting jitterMaxYaw = addSetting(new DoubleSetting("jitter horizontal", 1, 0, 3));
//    public final DoubleSetting jitterMaxPitch = addSetting(new DoubleSetting("jitter horizontal", 2, 0, 5));
    public final BoolSetting jitter = addSetting(new BoolSetting("Jitter", true));
    public final BoolSetting moveFix = addSetting(new BoolSetting("MoveFix", true));
    public final BoolSetting render = addSetting(new BoolSetting("Render", false));

    public RotationsFeature() {
        super("Rotations", "Client rotations configuration.", FeatureCategory.of("Client"), "rotate", "rotationSERVICE", "roate", "toationSERVICE");
        if (!this.isEnabled())
            this.toggle();
//        jitterSpeed.setShowCondition(() -> jitterAmount.get()>0);
//        jitterMaxYaw.setShowCondition(() -> jitterAmount.get() > 0);
//        jitterMaxPitch.setShowCondition(() -> jitterAmount.get() > 0);
        rotationSpeed.setShow(false);
        rotationEaseFactor.setShow(false);
        rotationThreshold.setShow(false);
        ticksBeforeRelease.setShow(false);
        render.setShow(false);
//        jitterAmount.setShow(false);
//        jitterSpeed.setShow(false);
//        jitterMaxYaw.setShow(false);
//        jitterMaxPitch.setShow(false);
    }
    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
