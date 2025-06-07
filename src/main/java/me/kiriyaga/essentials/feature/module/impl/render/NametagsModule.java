package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.util.EntityUtils;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.essentials.Essentials.*;

public class NametagsModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting showAnimals = addSetting(new BoolSetting("Peacefuls", true));
    public final BoolSetting showEnemies = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting showItems = addSetting(new BoolSetting("Items", true));

    public NametagsModule() {
        super("Nametags", "Draws nametags", "nametag");
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (MINECRAFT == null || MINECRAFT.world == null || MINECRAFT.player == null) return;

        if (showPlayers.get()) {
            for (PlayerEntity player : EntityUtils.getPlayers()) {
                if (player == MINECRAFT.player || player.isRemoved()) continue;
                drawNameTag(player, 0xFFFF5555);
            }
        }

        if (showAnimals.get()) {
            for (PassiveEntity animal : EntityUtils.getPassiveMobs()) {
                if (animal.isRemoved()) continue;
                drawNameTag(animal, 0xFFAAAAAA);
            }
        }

        if (showEnemies.get()) {
            for (HostileEntity hostile : EntityUtils.getHostileMobs()) {
                if (hostile.isRemoved()) continue;
                drawNameTag(hostile, 0xFFFF5555);
            }
        }

        if (showItems.get()) {
            for (ItemEntity item : EntityUtils.getDroppedItems()) {
                if (item.isRemoved()) continue;
                drawNameTag(item, 0xFFAAAAAA);
            }
        }
    }
    private void drawNameTag(Entity entity, int color) {
        Vec3d pos = entity.getPos().add(0, entity.getHeight() + 0.5, 0);
        Text text = entity.getName();
        float scale = 0.5f;

        RenderUtil.drawTextInWorld(
                MINECRAFT,
                text,
                pos,
                scale,
                color,
                true
        );
    }
}
