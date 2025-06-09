package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.EnumSetting;
import me.kiriyaga.essentials.util.EntityUtils;
import me.kiriyaga.essentials.util.NametagFormatter;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class NametagsModule extends Module {

    public final BoolSetting showPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting showAnimals = addSetting(new BoolSetting("Peacefuls", false));
    public final BoolSetting showEnemies = addSetting(new BoolSetting("Hostiles", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("Items", true));
    public final BoolSetting showHealth = addSetting(new BoolSetting("Show Health", true));
    public final EnumSetting<TextFormat> formatting = addSetting(new EnumSetting<>("Format", TextFormat.None));
    public final BoolSetting showBackground = addSetting(new BoolSetting("Background", true));

    private final NametagFormatter formatter = new NametagFormatter(this);

    public NametagsModule() {
        super("Nametags", "Draws names above entities", Category.RENDER, "nametag", "nmtags", "names", "тфьуефпы");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MINECRAFT == null || MINECRAFT.world == null || MINECRAFT.player == null) return;

        if (showPlayers.get()) {
            for (PlayerEntity player : EntityUtils.getPlayers()) {
                if (player == MINECRAFT.player || player.isRemoved()) continue;
                drawNameTag(player, formatter.formatPlayer(player), 0xFFFF5555, event.getPartialTicks());
            }
        }

        if (showAnimals.get()) {
            for (PassiveEntity animal : EntityUtils.getPassiveMobs()) {
                if (animal.isRemoved()) continue;
                drawNameTag(animal, formatter.formatEntity(animal), 0xFFAAAAAA, event.getPartialTicks());
            }
        }

        if (showEnemies.get()) {
            for (HostileEntity hostile : EntityUtils.getHostileMobs()) {
                if (hostile.isRemoved()) continue;
                drawNameTag(hostile, formatter.formatEntity(hostile), 0xFFFF5555, event.getPartialTicks());
            }
        }

        if (showItems.get()) {
            for (ItemEntity item : EntityUtils.getDroppedItems()) {
                if (item.isRemoved() || item.getStack().isEmpty()) continue;
                drawNameTag(item, formatter.formatItem(item), 0xFFAAAAAA, event.getPartialTicks());
            }
        }
    }

    private void drawNameTag(Entity entity, Text text, int color, float tickDelta) {
        double x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * tickDelta;
        double y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * tickDelta + entity.getHeight() + 0.5;
        double z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * tickDelta;

        Vec3d pos = new Vec3d(x, y, z);
        float scale = 0.5f;

        RenderUtil.drawTextInWorld(MINECRAFT, text, pos, scale, color, showBackground.get());
    }

    public enum TextFormat {
        None, Bold, Italic, Both
    }
}
