package me.kiriyaga.nami.impl.feature.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterFeature
public class AutoNametagFeature extends Feature {

    private final BoolSetting nametagged = addSetting(new BoolSetting("Nametagged", false));
    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 5.0, 1.0, 10.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 10, 1, 20));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    private int swapCooldown = 0;

    public AutoNametagFeature() {
        super("AutoNametag", "Automatically renames nearby entities with nametags.", FeatureCategory.of("World"), "nametag", "autoname", "autonametag");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity == null || entity == MC.player) continue;
            if (entity.getCustomName() != null && !nametagged.get()) continue;
            if (entity instanceof Villager || entity instanceof ThrownEnderpearl || entity instanceof EnderDragon) continue;

            int nameTagSlot = getNameTagSlot();
            if (nameTagSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != nameTagSlot) {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(nameTagSlot);
                swapCooldown = delay.get();
                return;
            }

            interactWithEntity(entity, range.get(), swing.get(), rotate.get(), this.name);

            swapCooldown = delay.get();
            break;
        }
    }

    private int getNameTagSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == Items.NAME_TAG) return i;
        }
        return -1;
    }
}