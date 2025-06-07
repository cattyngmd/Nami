package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Module;
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
    public final BoolSetting showAnimals = addSetting(new BoolSetting("Peacefuls", true));
    public final BoolSetting showEnemies = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting showItems = addSetting(new BoolSetting("Items", true));
    public final DoubleSetting lineWidth = addSetting(new DoubleSetting("Line", 1.0,0.5,2.0));
    public final BoolSetting filled = addSetting(new BoolSetting("filled", false));

    public ESPModule() {
        super("ESP", "Draws boxes around entities", "esp", "WH");
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (MINECRAFT == null || MINECRAFT.world == null || MINECRAFT.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        Vec3d cameraPos = MINECRAFT.gameRenderer.getCamera().getPos();

        if (showPlayers.get()) {
            for (PlayerEntity player : EntityUtils.getPlayers()) {
                if (player == MINECRAFT.player || player.isRemoved()) continue;
                drawBox(player, Color.RED, matrices, cameraPos);
            }
        }

        if (showAnimals.get()) {
            for (PassiveEntity animal : EntityUtils.getPassiveMobs()) {
                if (animal.isRemoved()) continue;
                drawBox(animal, Color.LIGHT_GRAY, matrices, cameraPos);
            }
        }

        if (showEnemies.get()) {
            for (HostileEntity hostile : EntityUtils.getHostileMobs()) {
                if (hostile.isRemoved()) continue;
                drawBox(hostile, Color.RED, matrices, cameraPos);
            }
        }

        if (showItems.get()) {
            for (ItemEntity item : EntityUtils.getDroppedItems()) {
                if (item.isRemoved()) continue;
                drawBox(item, Color.LIGHT_GRAY, matrices, cameraPos);
            }
        }
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, Vec3d cameraPos) {
        Box box = entity.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderUtil.drawBox(matrices, box, color, lineWidth.get());
    }
}
