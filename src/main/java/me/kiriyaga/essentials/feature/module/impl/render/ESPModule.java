package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.util.EntityUtils;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

        import static me.kiriyaga.essentials.Essentials.*;

public class ESPModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting showPeasefuls = addSetting(new BoolSetting("Peacefuls", true));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting showItems = addSetting(new BoolSetting("Items", true));
    public final DoubleSetting lineWidth = addSetting(new DoubleSetting("Line", 1.5,0.5,2.5));
    public final BoolSetting filled = addSetting(new BoolSetting("filled", false));

    public ESPModule() {
        super("ESP", "Draws boxes around entities", Category.RENDER, "esp", "WH", "boxes", "уыз");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MINECRAFT == null || MINECRAFT.world == null || MINECRAFT.player == null) return;

        MatrixStack matrices = event.getMatrices();
        ColorModule colorModule = MODULE_MANAGER.getModule(ColorModule.class);

        if (showPlayers.get()) {
            for (PlayerEntity player : EntityUtils.getPlayers()) {
                if (player == MINECRAFT.player || player.isRemoved()) continue;
                drawBox(player, colorModule.getStyledPrimaryColor(), matrices, event.getTickDelta());
            }
        }

        if (showPeasefuls.get()) {
            for (PassiveEntity animal : EntityUtils.getPassiveMobs()) {
                if (animal.isRemoved()) continue;
                drawBox(animal, Color.LIGHT_GRAY, matrices, event.getTickDelta());
            }
        }

        if (showHostiles.get()) {
            for (HostileEntity hostile : EntityUtils.getHostileMobs()) {
                if (hostile.isRemoved()) continue;
                drawBox(hostile, Color.RED, matrices, event.getTickDelta());
            }
        }

        if (showItems.get()) {
            for (ItemEntity item : EntityUtils.getDroppedItems()) {
                if (item.isRemoved()) continue;
                drawBox(item, Color.LIGHT_GRAY, matrices, event.getTickDelta());
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

        if (filled.get())
            RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), 122));
        else
            RenderUtil.drawBox(matrices, box, color, lineWidth.get());
    }


}
