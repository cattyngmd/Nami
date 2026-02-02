package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterFeature
public class AutoCowFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 2.5, 1.0, 5.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 5, 1, 20));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));

    private int swapCooldown = 0;

    public AutoCowFeature() {
        super("AutoCow", "Automatically milks nearby cows.", FeatureCategory.of("World"), "cow", "milk", "autocow");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE, 10, true)) {
            if (!(entity instanceof Cow cow)) continue;
            if (!cow.isAlive() || cow.isBaby()) continue;

            int bucketSlot = getBucketSlot();
            if (bucketSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != bucketSlot) {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(bucketSlot);
                swapCooldown = delay.get();
                return;
            }

            interactWithEntity(entity, range.get(), swing.get(), rotate.get(), this.name);
            swapCooldown = delay.get();
            break;
        }
    }

    private int getBucketSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == Items.BUCKET) return i;
        }
        return -1;
    }
}
