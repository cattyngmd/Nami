/*
Originally from:
https://github.com/NamiDevelopment/mint/blob/d8274468792503ccbfb1b374aaaeb74225a42056/src/main/java/net/melbourne/Features/impl/movement/StepFeature.java#L1

Licensed under MIT License
Copyright (c) 2026 Nami Development

https://github.com/NamiDevelopment/mint/blob/master/LICENSE
 */

package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class StepFeature extends Feature {

    private final DoubleSetting height = addSetting(new DoubleSetting("Height", 1.0, 0.5, 2.5));
    private final BoolSetting teleport = addSetting(new BoolSetting("Teleport", false));

    private double lastY;

    public StepFeature() {
        super("Step", "Steps up blocks (Normal mode).",
                FeatureCategory.of("Movement"), "step");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;
        this.clearDisplayInfo();

        this.addDisplayInfo(height.get()+"");
        double currentY = MC.player.getY();
        double stepHeight = currentY - lastY;

        if (stepHeight > 0.5 && stepHeight <= height.get()) {
            double[] offsets = getOffsets(stepHeight);
            if (offsets != null) {
                for (double offset : offsets) {
                    MC.player.connection.send(new ServerboundMovePlayerPacket.Pos(MC.player.getX(), lastY + offset, MC.player.getZ(), false, teleport.get()));
                }
            }
        }

        lastY = currentY;
    }

    private double[] getOffsets(double height) {
        if (Math.abs(height - 0.75) < 0.01) {
            return new double[]{0.42, 0.753, 1.0};
        } else if (Math.abs(height - 0.8125) < 0.01) {
            return new double[]{0.39, 0.7, 0.8125};
        } else if (Math.abs(height - 0.875) < 0.01) {
            return new double[]{0.39, 0.7, 0.875};
        } else if (Math.abs(height - 1.0) < 0.01) {
            return new double[]{0.42, 0.753, 1.0};
        } else if (Math.abs(height - 1.5) < 0.01) {
            return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
        } else if (Math.abs(height - 2.0) < 0.01) {
            return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        } else if (Math.abs(height - 2.5) < 0.01) {
            return new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
        }
        return null;
    }
}
