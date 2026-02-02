package namidevelopment.kiriyaga.nami.impl.feature.client;

import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.event.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;
import namidevelopment.kiriyaga.api.util.entity.EntityUtils;
import namidevelopment.kiriyaga.api.util.PredictMovementUtils;
import namidevelopment.kiriyaga.api.util.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
//@RegisterFeature
public class PredictTestFeature extends Feature {

    public final IntSetting ticks = addSetting(new IntSetting("Ticks", 3, 1, 20));
    public final BoolSetting predictSelf = addSetting(new BoolSetting("Self", true));
    public final BoolSetting predictOthers = addSetting(new BoolSetting("Others", true));
    public final BoolSetting showBox = addSetting(new BoolSetting("ShowBox", true));
    public final BoolSetting showEye = addSetting(new BoolSetting("ShowEye", true));

    public PredictTestFeature() {
        super("PredictTest", ".", FeatureCategory.of("Client"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC.level == null || MC.player == null) return;

        PoseStack matrices = event.getMatrices();

        if (predictSelf.get())
            renderPredictionForEntity(MC.player, matrices);

        if (predictOthers.get()) {
            List<Player> others = EntityUtils.getOtherPlayers();
            for (Player other : others) {
                if (other.isRemoved()) continue;
                renderPredictionForEntity(other, matrices);
            }
        }
    }

    private void renderPredictionForEntity(Entity entity, PoseStack matrices) {
        PredictMovementUtils.PredictedEntity initial = new PredictMovementUtils.PredictedEntity(
                entity.position(),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                entity.onGround(),
                entity.getEyeHeight()
        );

        PredictMovementUtils.PredictedEntity predicted = PredictMovementUtils.predict(initial, ticks.get(), t -> Vec3.ZERO);

        if (predicted == null) return;

        if (showBox.get()) {
            AABB box = entity.getBoundingBox().move(predicted.pos.subtract(entity.position()));
            RenderUtil.drawBoxLines(box, new Color(0, 255, 0, 200), true, true, 1.5f);

        }

        if (showEye.get()) {
            Vec3 eye = predicted.getEyePos();
            double size = 0.1;
            AABB eyeBox = new AABB(eye.x - size, eye.y - size, eye.z - size, eye.x + size, eye.y + size, eye.z + size);
            RenderUtil.drawBoxLines(eyeBox, new Color(255, 0, 0, 255), true, true, 1.5f);
        }
    }
}
