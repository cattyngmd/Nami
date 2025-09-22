package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.util.MatrixCache;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.NametagFormatter.*;

//@RegisterModule
public class TracersModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("peacefuls", false));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("neutrals", false));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("hostiles", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("items", false));
    public final DoubleSetting thickness = addSetting(new DoubleSetting("width", 1.5, 0.5, 2));

    public TracersModule() {
        super("tracers", "Draws lines from the center of the screen to entities.", ModuleCategory.of("visuals"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC.player == null || MC.world == null) return;

        MatrixStack matrices = event.getMatrices();
        float tracerThickness = this.thickness.get().floatValue();

        if (showPlayers.get()) {
            for (Entity player : ENTITY_MANAGER.getOtherPlayers()) {
                Color color = FRIEND_MANAGER.isFriend(player.getName().getString())
                        ? MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor()
                        : COLOR_HOSTILE;
                drawEntityTracer(matrices, player, color, tracerThickness);
            }
        }

        if (showPeacefuls.get()) {
            for (Entity entity : ENTITY_MANAGER.getPassive()) {
                drawEntityTracer(matrices, entity, COLOR_PASSIVE, tracerThickness);
            }
        }

        if (showNeutrals.get()) {
            for (Entity entity : ENTITY_MANAGER.getNeutral()) {
                drawEntityTracer(matrices, entity, COLOR_NEUTRAL, tracerThickness);
            }
        }

        if (showHostiles.get()) {
            for (Entity entity : ENTITY_MANAGER.getHostile()) {
                drawEntityTracer(matrices, entity, COLOR_HOSTILE, tracerThickness);
            }
        }

        if (showItems.get()) {
            for (Entity entity : ENTITY_MANAGER.getDroppedItems()) {
                drawEntityTracer(matrices, entity, COLOR_ITEM, tracerThickness);
            }
        }
    }

    private void drawEntityTracer(MatrixStack matrices, Entity entity, Color color, float thickness) {
        if (entity.isRemoved() || !entity.isAlive()) return;

        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        Vec3d camPos = MC.getEntityRenderDispatcher().camera.getPos();

        RenderUtil.drawLine(matrices, camPos, targetPos, color, thickness);
    }
}