package me.kiriyaga.nami.impl.feature.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;
import me.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterFeature
public class AutoSheepFeature extends Feature {

    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 2, 1.0, 5.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 5, 1, 20));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    private int swapCooldown = 0;

    public AutoSheepFeature() {
        super("AutoSheep", "Automatically shears nearby sheep.", FeatureCategory.of("World"), "sheep", "autowool");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE, 10, true)) {
            if (!(entity instanceof Sheep sheep)) continue;
            if (!sheep.isAlive() || sheep.isSheared() || sheep.isBaby()) continue;

            int shearsSlot = getShearsSlot();
            if (shearsSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != shearsSlot) {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(shearsSlot);
                swapCooldown = delay.get();
                return;
            }

            interactWithEntity(entity, range.get(), swing.get(), rotate.get(), this.name);

            swapCooldown = delay.get();
            break;
        }
    }

    private int getShearsSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == Items.SHEARS) return i;
        }
        return -1;
    }
}
