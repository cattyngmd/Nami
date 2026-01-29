package namidevelopment.kiriyaga.nami.impl.feature.impl.world;

import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.interactWithEntity;
import static namidevelopment.kiriyaga.nami.util.entity.EntityUtils.canBreed;

@RegisterFeature
public class AutoBreedFeature extends Feature {

    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 2, 1.0, 5.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 10, 1, 20));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));

    private final Set<Integer> animalsFed = new HashSet<>();
    private int breedCooldown = 0;

    public AutoBreedFeature() {
        super("AutoBreed", "Automatically breeds nearby animals.", FeatureCategory.of("World"), "autobreed");
    }

    @Override
    public void onDisable() {
        animalsFed.clear();
        breedCooldown = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        animalsFed.removeIf(id -> {
            Entity e = MC.level.getEntity(id);
            return e == null
                    || !e.isAlive()
                    || e instanceof Animal an && !canBreed(an);
        });

        if (breedCooldown > 0) {
            breedCooldown--;
            return;
        }

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE, 10, true)) {
            if (!(entity instanceof Animal animal)) continue;
            if (animalsFed.contains(animal.getId())) continue;

            if (!canBreed(animal)) continue;

            int foodSlot = getSlot(animal);
            if (foodSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != foodSlot) {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(foodSlot);
                breedCooldown = delay.get();
                return;
            }


            interactWithEntity(animal, range.get(), swing.get(), rotate.get(), this.name);

            animalsFed.add(animal.getId());
            breedCooldown = delay.get();
            break;
        }
    }

    private int getSlot(Animal animal) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && animal.isFood(stack)) {
                return i;
            }
        }
        return -1;
    }
}