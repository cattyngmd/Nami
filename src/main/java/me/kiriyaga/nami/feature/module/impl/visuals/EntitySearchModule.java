package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.ColorUtils;
import me.kiriyaga.nami.util.entity.EntityUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class EntitySearchModule extends Module {

    private final WhitelistSetting entityWhitelist = addSetting(new WhitelistSetting("Whitelist", true, WhitelistSetting.Type.ENTITY));
    private final BoolSetting renderBoxes = addSetting(new BoolSetting("Render", true));
    private final BoolSetting tracers = addSetting(new BoolSetting("Tracers", false));

    public EntitySearchModule() {
        super("EntitySearch", "Searchs for specified entities.", ModuleCategory.of("Render"));
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(Render3DEvent event) {
        if (MC.level == null || MC.player == null) return;

        Camera camera = MC.gameRenderer.getMainCamera();
        var camPos = camera.position();
        var start = camPos.add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()));

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.ALL)) {
            if (entity.isRemoved() || !entity.isAlive()) continue;

            if (!entityWhitelist.getWhitelist().contains(EntityType.getKey(entity.getType()))) continue;

            Color color = getColorForEntity(entity);

            double interpX = entity.xOld + (entity.getX() - entity.xOld) * event.getTickDelta();
            double interpY = entity.yOld + (entity.getY() - entity.yOld) * event.getTickDelta();
            double interpZ = entity.zOld + (entity.getZ() - entity.zOld) * event.getTickDelta();

            AABB box = entity.getBoundingBox().move(
                    interpX - entity.getX(),
                    interpY - entity.getY(),
                    interpZ - entity.getZ()
            );

            if (renderBoxes.get()) {
                RenderUtil.drawBoxLines(box, color, true, true, 1.5f);
            }

            if (tracers.get()) {
                RenderUtil.drawLine(start, box.getCenter(), color, 1.5f);
            }
        }
    }

    private Color getColorForEntity(Entity entity) {
        if (entity instanceof Player) {
            return MODULE_MANAGER.getStorage().getByClass(me.kiriyaga.nami.feature.module.impl.client.ColorModule.class).getStyledGlobalColor();
        }
        if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE).contains(entity)) return ColorUtils.COLOR_PASSIVE;
        if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.NEUTRAL).contains(entity)) return ColorUtils.COLOR_NEUTRAL;
        if (EntityUtils.getEntities(EntityUtils.EntityTypeCategory.HOSTILE).contains(entity)) return ColorUtils.COLOR_HOSTILE;
        if (entity instanceof ItemEntity) return ColorUtils.COLOR_ITEM;
        return Color.WHITE;
    }
}
