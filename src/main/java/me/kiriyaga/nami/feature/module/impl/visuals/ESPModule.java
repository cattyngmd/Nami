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
import me.kiriyaga.nami.util.ColorUtils;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LightLayer;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ESPModule extends Module {

    public enum RenderMode {OUTLINE, BOX}

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
        this.clearDisplayInfo();

        this.addDisplayInfo(renderMode.get().toString());


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
            return ColorUtils.COLOR_PASSIVE;
        } else if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL).contains(entity)) {
            return ColorUtils.COLOR_NEUTRAL;
        } else if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE).contains(entity)) {
            return ColorUtils.COLOR_HOSTILE;
        } else if (entity instanceof ItemEntity) {
            return ColorUtils.COLOR_ITEM;
        }
        return Color.WHITE;
    }

    private void renderItemBoxes(Render3DEvent event) {
        PoseStack matrices = event.getMatrices();
        float partialTicks = event.getTickDelta();

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.DROPPED_ITEMS)) {
            if (!showItems.get() || entity.isRemoved() || !entity.isAlive()) continue;

            Color color = ColorUtils.COLOR_ITEM;
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

        if (entity instanceof Player player) {
            if (!esp.showPlayers.get()) return null;
            return (FRIEND_MANAGER.isFriend(player.getName().getString()) ? MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getFriendColor() : MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor());
        }

        if (esp.showPeacefuls.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE).contains(entity)) return ColorUtils.COLOR_PASSIVE;
        if (esp.showNeutrals.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL).contains(entity)) return ColorUtils.COLOR_NEUTRAL;
        if (esp.showHostiles.get() && EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE).contains(entity)) return ColorUtils.COLOR_HOSTILE;
        if (entity instanceof ItemEntity) {
            if (!esp.showItems.get()) return null;
            if (esp.itemBoundingBox.get()) return null;
            return ColorUtils.COLOR_ITEM;
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
        BlockPos playerPos = MC.player.blockPosition();
        int lightLimit = mobSpawnLightThreshold.get();

        for (int x = -9; x <= 9; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -9; z <= 9; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);

                    if (canMobSpawn(pos, lightLimit)) {
                        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 0.1, pos.getZ() + 1.0);
                        RenderUtil.drawBoxLines(box, new Color(125, 125, 0, 255), true, true, 1.50f);
                    }

                }
            }
        }
    }
}
