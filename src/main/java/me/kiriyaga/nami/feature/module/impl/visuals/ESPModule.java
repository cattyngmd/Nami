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
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ESPModule extends Module {

    public static enum RenderMode {
        OUTLINE,
        BOX
    }

    public final BoolSetting showPlayers = addSetting(new BoolSetting("players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("peacefuls", true));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("neutrals", true));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("hostiles", true));
    public final BoolSetting showItems = addSetting(new BoolSetting("items", true));
    public final EnumSetting<RenderMode> renderMode = addSetting(new EnumSetting<>("mode", RenderMode.OUTLINE));
    public final BoolSetting smoothAppear = addSetting(new BoolSetting("smooth appearance", true));
    public final DoubleSetting lineWidth = addSetting(new DoubleSetting("line", 1.5, 0.5, 2.5));
    public final BoolSetting filled = addSetting(new BoolSetting("filled", false));

    private static final Color COLOR_PASSIVE = new Color(211, 211, 211, 255);
    private static final Color COLOR_NEUTRAL = new Color(255, 255, 0, 255);
    private static final Color COLOR_HOSTILE = new Color(255, 0, 0, 255);
    private static final Color COLOR_ITEM = new Color(211, 211, 211, 255);

    public ESPModule() {
        super("esp", "Highlights certain entities.", ModuleCategory.of("visuals"), "esp", "WH", "boxes", "уыз");
        smoothAppear.setShowCondition(() -> renderMode.get() == RenderMode.BOX);
        lineWidth.setShowCondition(() -> renderMode.get() == RenderMode.BOX);
        filled.setShowCondition(() -> renderMode.get() == RenderMode.BOX);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC == null || MC.world == null || MC.player == null) return;

        if (renderMode.get() == RenderMode.BOX) {
            renderBoxes(event);
        }
    }

    private void renderBoxes(Render3DEvent event) {
        MatrixStack matrices = event.getMatrices();
        float partialTicks = event.getTickDelta();
        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);

        for (Entity entity : getEntitiesToRender()) {
            if (entity.isRemoved() || !entity.isAlive()) continue;

            Color color = getColorForEntity(entity, colorModule);
            drawBox(entity, color, matrices, partialTicks);
        }
    }

    private Set<Entity> getEntitiesToRender() {
        Set<Entity> entities = new HashSet<>();

        if (showPlayers.get()) entities.addAll(ENTITY_MANAGER.getOtherPlayers());
        if (showPeacefuls.get()) entities.addAll(ENTITY_MANAGER.getPassive());
        if (showNeutrals.get()) entities.addAll(ENTITY_MANAGER.getNeutral());
        if (showHostiles.get()) entities.addAll(ENTITY_MANAGER.getHostile());
        if (showItems.get()) entities.addAll(ENTITY_MANAGER.getDroppedItems());

        return entities;
    }

    private Color getColorForEntity(Entity entity, ColorModule colorModule) {
        if (entity instanceof PlayerEntity) {
            if (FRIEND_MANAGER.isFriend(entity.getName().getString())) {
                return colorModule.getStyledGlobalColor();
            } else {
                return COLOR_HOSTILE;
            }
        } else if (ENTITY_MANAGER.getPassive().contains(entity)) {
            return COLOR_PASSIVE;
        } else if (ENTITY_MANAGER.getNeutral().contains(entity)) {
            return COLOR_NEUTRAL;
        } else if (ENTITY_MANAGER.getHostile().contains(entity)) {
            return COLOR_HOSTILE;
        } else if (entity instanceof ItemEntity) {
            return COLOR_ITEM;
        }
        return Color.WHITE;
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

        int alphaCoef = smoothAppear.get() ? Math.min(entity.age * 10, 255) : 255;

        if (filled.get()) {
            int filledAlpha = Math.min(alphaCoef, 75);
            RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), filledAlpha));
            RenderUtil.drawBox(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaCoef), lineWidth.get());
        } else {
            RenderUtil.drawBox(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaCoef), lineWidth.get());
        }
    }

    public static Color getESPColor(Entity entity) {
        ESPModule esp = MODULE_MANAGER.getStorage().getByClass(ESPModule.class);
        if (esp == null || !esp.isEnabled()) return null;

        if (entity == null || entity.isRemoved() || !entity.isAlive()) return null;

        if (entity instanceof PlayerEntity) {
            if (!esp.showPlayers.get()) return null;
            if (FRIEND_MANAGER.isFriend(entity.getName().getString()))
                return MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
            else
                return COLOR_HOSTILE;
        }

        if (esp.showPeacefuls.get() && ENTITY_MANAGER.getPassive().contains(entity)) return COLOR_PASSIVE;
        if (esp.showNeutrals.get() && ENTITY_MANAGER.getNeutral().contains(entity)) return COLOR_NEUTRAL;
        if (esp.showHostiles.get() && ENTITY_MANAGER.getHostile().contains(entity)) return COLOR_HOSTILE;
        if (esp.showItems.get() && entity instanceof ItemEntity) return COLOR_ITEM;

        return null;
    }
}
