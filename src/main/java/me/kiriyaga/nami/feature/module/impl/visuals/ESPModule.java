package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.entity.EntityUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.NametagFormatter.*;

@RegisterModule
public class ESPModule extends Module {

    public static enum RenderMode {
        OUTLINE,
        BOX
    }

    public final BoolSetting showPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("Peacefuls", true));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("Hostiles", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("Items", true));
    public final BoolSetting itemBoundingBox = addSetting(new BoolSetting("ItemBoundingBox", true));
    public final BoolSetting showMobSpawns = addSetting(new BoolSetting("MobSpawn", false));
    public final IntSetting mobSpawnLightThreshold = addSetting(new IntSetting("SpawnLight", 7, 0, 15));
    public final EnumSetting<RenderMode> renderMode = addSetting(new EnumSetting<>("Mode", RenderMode.OUTLINE));
    public final DoubleSetting outlineDistance = addSetting(new DoubleSetting("Distance", 52, 15, 256));

    public ESPModule() {
        super("ESP", "Highlights certain entities.", ModuleCategory.of("Render"), "esp", "wh", "boxes");
        outlineDistance.setShowCondition(() -> renderMode.get() == RenderMode.OUTLINE);
        itemBoundingBox.setShowCondition(() -> showItems.get());
        mobSpawnLightThreshold.setShowCondition(showMobSpawns::get);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC == null || MC.level == null || MC.player == null) return;

        this.setDisplayInfo(renderMode.get().toString());


        if (showMobSpawns.get())
            renderMob(event);

        if (renderMode.get() == RenderMode.BOX) {
            renderBoxes(event);
            return;
        }

        if (renderMode.get() == RenderMode.OUTLINE) {
            if (itemBoundingBox.get()) {
                renderItemBoxes(event);
            }
        }
    }

    private void renderBoxes(Render3DEvent event) {
        PoseStack matrices = event.getMatrices();
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

        if (showPlayers.get()) entities.addAll(EntityUtils.getOtherPlayers());
        if (showPeacefuls.get()) entities.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE));
        if (showNeutrals.get()) entities.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL));
        if (showHostiles.get()) entities.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE));
        if (showItems.get()) entities.addAll(EntityUtils.getEntities(EntityUtils.EntityTypeCategory.DROPPED_ITEMS));

        if (renderMode.get() == RenderMode.OUTLINE) {
            double maxDistSq = outlineDistance.get() * outlineDistance.get();
            entities.removeIf(entity -> MC.player.distanceToSqr(entity) > maxDistSq);
        }

        return entities;
    }

    private Color getColorForEntity(Entity entity, ColorModule colorModule) {
        if (entity instanceof Player) {
            return colorModule.getStyledGlobalColor();
        } else if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE).contains(entity)) {
            return COLOR_PASSIVE;
        } else if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL).contains(entity)) {
            return COLOR_NEUTRAL;
        } else if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE).contains(entity)) {
            return COLOR_HOSTILE;
        } else if (entity instanceof ItemEntity) {
            return COLOR_ITEM;
        }
        return Color.WHITE;
    }

    private void renderItemBoxes(Render3DEvent event) {
        PoseStack matrices = event.getMatrices();
        float partialTicks = event.getTickDelta();

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.DROPPED_ITEMS)) {
            if (!showItems.get() || entity.isRemoved() || !entity.isAlive()) continue;

            Color color = COLOR_ITEM;
            drawBox(entity, color, matrices, partialTicks);
        }
    }

    private void drawBox(Entity entity, Color color, PoseStack matrices, float partialTicks) {
        double interpX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
        double interpY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks;
        double interpZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;

        AABB box = entity.getBoundingBox().move(
                interpX - entity.getX(),
                interpY - entity.getY(),
                interpZ - entity.getZ()
        );
        RenderUtil.drawBoxLines(box, color, true, true, 1.5f);

    }

    public static Color getESPColor(Entity entity) {
        ESPModule esp = MODULE_MANAGER.getStorage().getByClass(ESPModule.class);
        if (esp == null || !esp.isEnabled()) return null;

        double d = MODULE_MANAGER.getStorage().getByClass(ESPModule.class).outlineDistance.get();

        if (MC.getCameraEntity().distanceTo(entity) > d)
            return null;

        if (entity == null || entity.isRemoved() || !entity.isAlive()) return null;

        if (entity instanceof Player) {
            if (!esp.showPlayers.get()) return null;
            return MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
        }

        if (esp.showPeacefuls.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE).contains(entity)) return COLOR_PASSIVE;
        if (esp.showNeutrals.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL).contains(entity)) return COLOR_NEUTRAL;
        if (esp.showHostiles.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE).contains(entity)) return COLOR_HOSTILE;
        if (entity instanceof ItemEntity) {
            if (!esp.showItems.get()) return null;
            if (esp.itemBoundingBox.get()) return null;
            return COLOR_ITEM;
        }
        return null;
    }

    public static boolean canMobSpawn(BlockPos pos, int spawnLightLimit) {
        BlockState state = MC.level.getBlockState(pos);
        BlockState below = MC.level.getBlockState(pos.below());
        Block blockBelow = below.getBlock();

        boolean isSnowLayer = state.getBlock() instanceof SnowLayerBlock && state.getValue(SnowLayerBlock.LAYERS) == 1;
        if (!state.isAir() && !isSnowLayer) return false;
        if (blockBelow == Blocks.BEDROCK || blockBelow == Blocks.BARRIER || blockBelow instanceof TransparentBlock || blockBelow instanceof ScaffoldingBlock)
            return false;

        if (!(blockBelow == Blocks.SOUL_SAND || blockBelow == Blocks.MUD || (blockBelow instanceof SlabBlock && below.getValue(SlabBlock.TYPE) == SlabType.TOP) || (blockBelow instanceof StairBlock && below.getValue(StairBlock.HALF) == Half.TOP) || below.isSolidRender()))
            return false;

        int block = MC.level.getBrightness(LightLayer.BLOCK, pos);
        //int sky = MC.world.getLightLevel(LightType.SKY, pos);

        return block <= spawnLightLimit;
    }

    private void renderMob(Render3DEvent event) {
        PoseStack matrices = event.getMatrices();
        Level world = MC.level;
        BlockPos playerPos = MC.player.blockPosition();
        int lightLimit = mobSpawnLightThreshold.get();

        for (int x = -9; x <= 9; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -9; z <= 9; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);

                    if (canMobSpawn(pos, lightLimit)) {
                        int blockLight = world.getMaxLocalRawBrightness(pos);

                        double renderX = pos.getX() + 0.5;
                        double renderY = pos.getY() + 1.0;
                        double renderZ = pos.getZ() + 0.5;
                        Vec3 camPos = MC.gameRenderer.getMainCamera().position();

                        float distance = (float) camPos.distanceTo(new Vec3(renderX, renderY, renderZ));
                        int scale = 30;

                        float dynamicScale = 0.0018f + (scale / 10000.0f) * distance;
                        if (distance <= 8.0f) dynamicScale = 0.0245f;

                        Vec3 renderPos = new Vec3(renderX, renderY, renderZ);
                        Component text = Component.nullToEmpty(String.valueOf(blockLight));
                        RenderUtil.drawText3D(matrices, text, renderPos, dynamicScale, false, false, 1);
                    }
                }
            }
        }
    }
}
