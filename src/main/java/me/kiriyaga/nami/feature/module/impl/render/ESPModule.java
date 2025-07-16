package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;

public class ESPModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("peacefuls", true));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("neutrals", true));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("hostiles", true));
    public final BoolSetting showItems = addSetting(new BoolSetting("items", true));
    public final BoolSetting smoothAppear = addSetting(new BoolSetting("smooth appearance", true));
    public final DoubleSetting lineWidth = addSetting(new DoubleSetting("line", 1.5, 0.5, 2.5));
    public final BoolSetting filled = addSetting(new BoolSetting("filled", false));

    private static final Color COLOR_PASSIVE = new Color(211, 211, 211, 255);
    private static final Color COLOR_NEUTRAL = new Color(255, 255, 0, 255);
    private static final Color COLOR_HOSTILE = new Color(255, 0, 0, 255);
    private static final Color COLOR_ITEM = new Color(211, 211, 211, 255);

    public ESPModule() {
        super("esp", "Draws boxes around certain entities.", Category.visuals, "esp", "WH", "boxes", "уыз");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MINECRAFT == null || MINECRAFT.world == null || MINECRAFT.player == null) return;

        MatrixStack matrices = event.getMatrices();
        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);

        if (showPlayers.get()) {
            for (Entity player : ENTITY_MANAGER.getOtherPlayers()) {
                if (player.isRemoved()) continue;
                if (FRIEND_MANAGER.isFriend(player.getName().getString()))
                    drawBox(player, colorModule.getStyledGlobalColor(), matrices, event.getTickDelta());
                else
                    drawBox(player, COLOR_HOSTILE, matrices, event.getTickDelta());
            }
        }

        if (showPeacefuls.get()) {
            for (Entity entity : ENTITY_MANAGER.getPassive()) {
                if (!entity.isAlive()) continue;
                drawBox(entity, COLOR_PASSIVE, matrices, event.getTickDelta());
            }
        }

        if (showNeutrals.get()) {
            for (Entity entity : ENTITY_MANAGER.getNeutral()) {
                if (!entity.isAlive()) continue;
                drawBox(entity, COLOR_NEUTRAL, matrices, event.getTickDelta());
            }
        }

        if (showHostiles.get()) {
            for (Entity entity : ENTITY_MANAGER.getHostile()) {
                if (!entity.isAlive()) continue;
                drawBox(entity, COLOR_HOSTILE, matrices, event.getTickDelta());
            }
        }

        if (showItems.get()) {
            for (ItemEntity item :  ENTITY_MANAGER.getDroppedItems()) {
                if (item.isRemoved()) continue;
                drawBox(item, COLOR_ITEM, matrices, event.getTickDelta());
            }
        }
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, float partialTicks) {
        double interpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks;
        double interpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks;
        double interpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks;

        Box box = entity.getBoundingBox().offset(
                interpX - entity.getX(),
                interpY - entity.getY(),
                interpZ - entity.getZ()
        );

        int cof = smoothAppear.get() ? entity.age : 99;

        int alpha = Math.min(cof * 10, 255);

        if (filled.get()) {
            int filledAlpha = Math.min(alpha, 75);
            RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), filledAlpha));
        } else {
            RenderUtil.drawBox(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha), lineWidth.get());
        }
    }

}
