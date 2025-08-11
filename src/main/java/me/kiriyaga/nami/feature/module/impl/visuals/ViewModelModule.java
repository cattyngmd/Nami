package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.HeldItemRendererEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class ViewModelModule extends Module {

    public final DoubleSetting scale = addSetting(new DoubleSetting("scale", 1.0, 0.1, 1.6));
    public final DoubleSetting posX = addSetting(new DoubleSetting("posX", 0.0, -0.6, 0.6));
    public final DoubleSetting posY = addSetting(new DoubleSetting("posY", 0.0, -0.6, 0.6));
    public final DoubleSetting posZ = addSetting(new DoubleSetting("posZ", 0.0, -0.6, 0.6));
    public final DoubleSetting rotX = addSetting(new DoubleSetting("rotX", 0.0, -180.0, 180.0));
    public final DoubleSetting rotY = addSetting(new DoubleSetting("rotY", 0.0, -180.0, 180.0));
    public final DoubleSetting rotZ = addSetting(new DoubleSetting("rotZ", 0.0, -180.0, 180.0));

    public ViewModelModule() {
        super("viewmodel", "Modifies hand position, scale, and rotation.", ModuleCategory.of("visuals"), "vm", "handpos", "мшуцьщвуд");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onHeldItemRender(HeldItemRendererEvent ev) {
        boolean isOffhand = ev.getHand() == Hand.OFF_HAND;
        float mirror = isOffhand ? -1.0f : 1.0f;

        ev.getMatrix().translate(
                posX.get() * mirror,
                posY.get(),
                posZ.get()
        );

        float rx = rotX.get().floatValue();
        float ry = rotY.get().floatValue() * mirror;
        float rz = rotZ.get().floatValue();

        ev.getMatrix().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rx));
        ev.getMatrix().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ry));
        ev.getMatrix().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rz));

        ev.getMatrix().scale(
                 (scale.get().floatValue()),
                 scale.get().floatValue(),
                scale.get().floatValue()
        );
    }
}
