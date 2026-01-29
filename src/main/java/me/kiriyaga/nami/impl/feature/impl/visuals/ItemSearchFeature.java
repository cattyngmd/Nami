package me.kiriyaga.nami.impl.feature.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.entity.EntityUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.ColorUtils.COLOR_ITEM;

@RegisterFeature
public class ItemSearchFeature extends Feature {

    private final WhitelistSetting itemWhitelist = addSetting(new WhitelistSetting("Whitelist", true, WhitelistSetting.Type.ENTITY));
    private final BoolSetting renderBoxes = addSetting(new BoolSetting("Render", true));
    private final BoolSetting tracers = addSetting(new BoolSetting("Tracers", false));
    private final BoolSetting itemFrames = addSetting(new BoolSetting("ItemFrames", false));
    private final BoolSetting chatFeedback = addSetting(new BoolSetting("ChatFeedback", false));

    private final Set<Integer> sent = new HashSet<>();
    public ItemSearchFeature() {
        super("ItemSearch", "Searches for specified item entities.", FeatureCategory.of("Render"));
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(Render3DEvent event) {
        if (MC.level == null || MC.player == null) return;

        Camera camera = MC.gameRenderer.getMainCamera();
        Vec3 camPos = camera.position();
        Vec3 start = camPos.add(Vec3.directionFromRotation(camera.xRot(), camera.yRot()));

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.DROPPED_ITEMS)) {
            if (entity instanceof ItemEntity item) {
                if (item.isRemoved() || !item.isAlive()) continue;


                Identifier id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem());

                if (!itemWhitelist.getWhitelist().contains(id)) continue;

                double interpX = item.xOld + (item.getX() - item.xOld) * event.getTickDelta();
                double interpY = item.yOld + (item.getY() - item.yOld) * event.getTickDelta();
                double interpZ = item.zOld + (item.getZ() - item.zOld) * event.getTickDelta();

                AABB box = item.getBoundingBox().move(interpX - item.getX(), interpY - item.getY(), interpZ - item.getZ());

                if (renderBoxes.get()) {
                    RenderUtil.drawBoxLines(box, COLOR_ITEM, true, true, 1.5f);
                }

                if (tracers.get()) {
                    RenderUtil.drawLine(start, box.getCenter(), COLOR_ITEM, 1.5f);
                }

                if (chatFeedback.get()) {
                    Integer entId = entity.getId();
                    Component message = CAT_FORMAT.format("Item: {g}" + item.getItem().getHoverName().getString() + " {reset} found.");
                    CHAT_SERVICE.sendPersistent(entId.toString(), message);
                    sent.add(entId);
                }
            }
        }
        if (itemFrames.get()) {
            for (Entity entity : MC.level.entitiesForRendering()) {
                if (!(entity instanceof ItemFrame frame)) continue;

                if (frame.getItem().isEmpty()) continue;

                Identifier id = BuiltInRegistries.ITEM.getKey(frame.getItem().getItem());
                if (!itemWhitelist.getWhitelist().contains(id)) continue;
                double interpX = frame.xOld + (frame.getX() - frame.xOld) * event.getTickDelta();
                double interpY = frame.yOld + (frame.getY() - frame.yOld) * event.getTickDelta();
                double interpZ = frame.zOld + (frame.getZ() - frame.zOld) * event.getTickDelta();

                AABB box = frame.getBoundingBox().move(interpX - frame.getX(), interpY - frame.getY(), interpZ - frame.getZ());

                if (renderBoxes.get()) {
                    RenderUtil.drawBoxLines(box, COLOR_ITEM, true, true, 1.5f);
                }

                if (tracers.get()) {
                    RenderUtil.drawLine(start, box.getCenter(), COLOR_ITEM, 1.5f);
                }

                if (chatFeedback.get()) {
                    Integer entId = entity.getId();
                    Component message = CAT_FORMAT.format("Item: {g}" + frame.getItem().getItemName() + " {reset} found.");
                    CHAT_SERVICE.sendPersistent(entId.toString(), message);
                    sent.add(entId);
                }
            }
        }
    }
}
