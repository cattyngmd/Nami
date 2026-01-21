/*
Originally from:
https://github.com/NamiDevelopment/mint/blob/d8274468792503ccbfb1b374aaaeb74225a42056/src/main/java/net/melbourne/modules/impl/render/NametagsFeature.java

Licensed under MIT License
Copyright (c) 2026 Nami Development

https://github.com/NamiDevelopment/mint/blob/master/LICENSE
 */

package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.util.ColorUtils;
import me.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.entity.EntityUtils.getRenderPos;
import static me.kiriyaga.nami.util.render.RenderUtil.project;
import static me.kiriyaga.nami.util.render.RenderUtil.projectionVisible;

@RegisterModule
public class NametagsModule extends Module {

    public final BoolSetting self = addSetting(new BoolSetting("Self", false));
    public final BoolSetting invisible = addSetting(new BoolSetting("Invisibles", true));
    public final BoolSetting scaling = addSetting(new BoolSetting("Scaling", true));
    public final BoolSetting gameMode = addSetting(new BoolSetting("GameMode", false));
    public final BoolSetting ping = addSetting(new BoolSetting("Ping", true));
    public final BoolSetting entityId = addSetting(new BoolSetting("EntityID", false));
    public final BoolSetting health = addSetting(new BoolSetting("Health", true));
    public final BoolSetting totemPops = addSetting(new BoolSetting("TotemPops", false));
    public final BoolSetting armor = addSetting(new BoolSetting("Armor", true));
    public final BoolSetting durability = addSetting(new BoolSetting("Durability", true));
    public final BoolSetting rectangle = addSetting(new BoolSetting("Background", true));
    public final BoolSetting items = addSetting(new BoolSetting("Items", false));
    public final BoolSetting pearls = addSetting(new BoolSetting("Pearls", true));

    //0, 0, 0, 100
    //19, 19, 19, 140

    public NametagsModule() {
        super("Nametags", "Draws nametags above certain entities.", ModuleCategory.of("Render"));
    }

    @SubscribeEvent
    public void onRenderOverlay(Render2DEvent event) {
        if (MC.level == null) return;


        Matrix3x2fStack matrices = event.getDrawContext().pose();

        for (Entity ent : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PLAYERS)) {
            if (ent == MC.player && !self.get()) continue;

            if (ent instanceof Player player) {

                Vec3 vec3d = getRenderPos(player, MC.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                Vec3 projected = project(vec3d.add(0, (player.isShiftKeyDown() ? 1.9f : 2.1f), 0));

                if (!invisible.get() && player.isInvisible()) continue;
                if (!player.isAlive()) continue;
                if (!projectionVisible(projected)) continue;

                String ign = player.getName().getString();

                String text = "";

                if (gameMode.get()) {
                    String gm = EntityUtils.getGameMode(player).getName().toUpperCase();
                    text += "[" + (gm.isEmpty() ? "" : gm.substring(0, 1)) + "]";
                }

                text += ign;

                if (ping.get())
                    text += " " + EntityUtils.getLatency(player) + "ms";

                if (entityId.get())
                    text += " " + player.getId();

                if (health.get())
                    text += " " + EntityUtils.getHealthNumber(ent);

                float width = FONT_MANAGER.getWidth(text);

                String colored = FRIEND_MANAGER.isFriend(ign) ? "{g}" + text : text;

                if (health.get())
                    colored = colored.replace(
                            " " + EntityUtils.getHealthNumber(ent),
                            " " + ColorUtils.getHealthColor(ent) + EntityUtils.getHealthNumber(ent)
                    );

                Component display = CAT_FORMAT.format(colored);


                float scale = 1.0f;
                if (scaling.get()) {
                    float dist = MC.getCameraEntity().distanceTo(ent);
                    scale = Math.max(0.5f, Math.min(1.0f, 20.0f / dist));
                }

                matrices.pushMatrix();
                matrices.translate((float) projected.x, (float) projected.y);
                matrices.scale(scale, scale);

                if (rectangle.get()) {
                    GuiGraphics ctx = event.getDrawContext();
                    int x1 = (int) (-width / 2f - 1);
                    int y1 = (int) (-FONT_MANAGER.getHeight() - 2);
                    int x2 = (int) (width / 2 + 2);
                    int y2 = 0;
                    ctx.fill(x1, y1, x2, y2, 0x64000000); // 0x64 = 100 alpha
                    int outlineColor = (140 << 24) | (19 << 16) | (19 << 8) | 19;
                    ctx.fill(x1, y1, x2, y1 + 1, outlineColor);
                    ctx.fill(x1, y2 - 1, x2, y2, outlineColor);
                    ctx.fill(x1, y1, x1 + 1, y2, outlineColor);
                    ctx.fill(x2 - 1, y1, x2, y2, outlineColor);
                }

                FONT_MANAGER.drawText(event.getDrawContext(), display, (int) (-FONT_MANAGER.getWidth(text) / 2.f), -FONT_MANAGER.getHeight(), true);

                if (armor.get()) {
                    List<ItemStack> stacks = new ArrayList<>();
                    ItemStack[] all = new ItemStack[]{
                            ((Player) ent).getItemBySlot(EquipmentSlot.MAINHAND),
                            ((Player) ent).getItemBySlot(EquipmentSlot.FEET),
                            ((Player) ent).getItemBySlot(EquipmentSlot.LEGS),
                            ((Player) ent).getItemBySlot(EquipmentSlot.CHEST),
                            ((Player) ent).getItemBySlot(EquipmentSlot.HEAD),
                            ((Player) ent).getItemBySlot(EquipmentSlot.OFFHAND)
                    };

                    for (ItemStack stack : all) {
                        if (!stack.isEmpty()) stacks.add(stack);
                    }

                    if (!stacks.isEmpty()) {
                        int totalWidth = (stacks.size() * 16) + ((stacks.size() - 1) * 2);
                        int x = -totalWidth / 2;
                        int y = -30;

                        for (int i = stacks.size() - 1; i >= 0; i--) {
                            ItemStack stack = stacks.get(i);
                            event.getDrawContext().renderItem(stack, x, y);
                            event.getDrawContext().renderItemDecorations(FONT_MANAGER.rendererProvider.getRenderer(), stack, x, y);

                            if (durability.get()) {
                                int damage = stack.getDamageValue();
                                int maxDamage = stack.getMaxDamage();

                                if (maxDamage > 0) {
                                    event.getDrawContext().pose().pushMatrix();
                                    event.getDrawContext().pose().translate(x + 8 - (FONT_MANAGER.getWidth((((maxDamage - damage) * 100) / maxDamage) + "%") * 0.75f) / 2.0F, y - (6 * 0.75f));
                                    event.getDrawContext().pose().pushMatrix();
                                    event.getDrawContext().pose().scale(0.75f, 0.75f);
                                    float ratio = (maxDamage - damage) / (float) maxDamage;
                                    int color = new Color(1.0f - ratio, ratio, 0).getRGB();

                                    FONT_MANAGER.drawText(event.getDrawContext(), (((maxDamage - damage) * 100) / maxDamage) + "%", 0, 0, true, color);
                                    event.getDrawContext().pose().popMatrix();
                                    event.getDrawContext().pose().popMatrix();
                                }
                            }

                            x += 16 + 2;
                        }
                    }
                }

                matrices.popMatrix();
            }
        }

        if (items.get()) {
            for (Entity enttt : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.DROPPED_ITEMS)) {
                Vec3 pos = getRenderPos(enttt, MC.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                pos = pos.add(0, 0.2, 0);
                Vec3 proj = project(pos);
                if (!projectionVisible(proj)) continue;

                if (enttt instanceof ItemEntity item) {
                    ItemStack stack = item.getItem();
                    if (stack.isEmpty()) continue;

                    String name = stack.getItemName().getString();
                    int count = stack.getCount();
                    String display = name + (count > 1 ? " x" + count : "");

                    float scale = 1.0f;
                    if (scaling.get()) {
                        float dist = MC.getCameraEntity().distanceTo(item);
                        scale = Math.max(0.5f, Math.min(1.0f, 20.0f / dist));
                    }

                    matrices.pushMatrix();
                    matrices.translate((float) proj.x, (float) proj.y);
                    matrices.scale(scale, scale);
                    FONT_MANAGER.drawText(event.getDrawContext(), display, -FONT_MANAGER.getWidth(display) / 2, -FONT_MANAGER.getHeight(), true, Color.white.getRGB());
                    matrices.popMatrix();
                }
            }
        }

        if (pearls.get()) {
            for (Entity entt : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.ALL)) {
                if (entt instanceof ThrownEnderpearl pearl) {
                    Vec3 pos = getRenderPos(pearl, MC.getDeltaTracker().getGameTimeDeltaPartialTick(true));
                    Vec3 proj = project(pos.add(0, 0.25, 0));

                    if (!projectionVisible(proj)) continue;

                    if (pearl.getOwner() instanceof Player thrower) {
                        String display = thrower.getName().getString();

                        float scale = 1.0f;
                        if (scaling.get()) {
                            float dist = MC.getCameraEntity().distanceTo(pearl);
                            scale = Math.max(0.5f, Math.min(1.0f, 20.0f / dist));
                        }

                        matrices.pushMatrix();
                        matrices.translate((float) proj.x, (float) proj.y);
                        matrices.scale(scale, scale);
                        FONT_MANAGER.drawText(event.getDrawContext(), display, -FONT_MANAGER.getWidth(display) / 2, -FONT_MANAGER.getHeight(), true, Color.white.getRGB());
                        matrices.popMatrix();
                    }
                }
            }
        }
    }
}
