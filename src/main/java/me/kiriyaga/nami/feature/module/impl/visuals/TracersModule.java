package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.util.MatrixCache;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;

//@RegisterModule
public class TracersModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("peacefuls", false));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("neutrals", false));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("hostiles", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("items", false));
    public final DoubleSetting thickness = addSetting(new DoubleSetting("thickness", 0.002, 0.0005, 0.002));

    private static final Color COLOR_PASSIVE = new Color(211, 211, 211, 255);
    private static final Color COLOR_NEUTRAL = new Color(255, 255, 0, 255);
    private static final Color COLOR_HOSTILE = new Color(255, 0, 0, 255);
    private static final Color COLOR_ITEM = new Color(211, 211, 211, 255);

    public TracersModule() {
        super("tracers", "Draws lines from the center of the screen to entities.", ModuleCategory.of("visuals"), "екфсукы");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC == null || MC.player == null || MC.world == null) return;
        if (MatrixCache.camera == null) return;

        Vec3d cameraPos = MatrixCache.camera.getPos();
        Quaternionf rotation = MatrixCache.camera.getRotation();
        Vector3f forward = new Vector3f(0, 0, -1);
        Vector3f lookDir = rotation.transform(forward);
        Vec3d lookVec = new Vec3d(lookDir.x(), lookDir.y(), lookDir.z());

        double tracerStartOffset = 0.4;
        Vec3d tracerStart = cameraPos.add(lookVec.multiply(tracerStartOffset));

        MatrixStack matrices = event.getMatrices();

        float tracerThickness = this.thickness.get().floatValue();

        java.util.function.BiConsumer<Entity, Color> drawEntityTracer = (entity, color) -> {
            if (entity.isRemoved() || !entity.isAlive()) return;

            Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);

            int argb = ((color.getAlpha() & 0xFF) << 24) |
                    ((color.getRed() & 0xFF) << 16) |
                    ((color.getGreen() & 0xFF) << 8) |
                    (color.getBlue() & 0xFF);

            //CHAT_MANAGER.sendRaw("drawline3d");
            RenderUtil.drawThickLine(matrices, tracerStart, targetPos, tracerThickness, argb);
        };

        if (showPlayers.get()) {
            for (Entity player : ENTITY_MANAGER.getOtherPlayers()) {
                Color color = FRIEND_MANAGER.isFriend(player.getName().getString())
                        ? MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor()
                        : COLOR_HOSTILE;
                drawEntityTracer.accept(player, color);
            }
        }

        if (showPeacefuls.get()) {
            for (Entity entity : ENTITY_MANAGER.getPassive()) {
                drawEntityTracer.accept(entity, COLOR_PASSIVE);
            }
        }

        if (showNeutrals.get()) {
            for (Entity entity : ENTITY_MANAGER.getNeutral()) {
                drawEntityTracer.accept(entity, COLOR_NEUTRAL);
            }
        }

        if (showHostiles.get()) {
            for (Entity entity : ENTITY_MANAGER.getHostile()) {
                drawEntityTracer.accept(entity, COLOR_HOSTILE);
            }
        }

        if (showItems.get()) {
            for (Entity entity : ENTITY_MANAGER.getDroppedItems()) {
                drawEntityTracer.accept(entity, COLOR_ITEM);
            }
        }
    }
}
