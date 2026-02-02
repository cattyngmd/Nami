package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterFeature
public class AutoMountFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 2, 1.0, 10.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 10, 1, 20));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));

    private int actionCooldown = 0;

    public AutoMountFeature() {
        super("AutoMount", "Automatically mounts nearby entities.", FeatureCategory.of("World"), "mount", "automount");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (MC.player.isPassenger()) return;

        if (actionCooldown > 0) {
            actionCooldown--;
            return;
        }

        for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity == null || entity == MC.player || !entity.isAlive() || entity.isVehicle()) continue;

            if (!(entity instanceof Horse || entity instanceof Pig || entity instanceof Strider ||
                    entity instanceof Llama || entity instanceof Donkey ||
                    entity instanceof Boat || entity instanceof Minecart)) continue;

            interactWithEntity(entity, range.get(), swing.get(), rotate.get(), this.name);

            actionCooldown = delay.get();
            break;
        }
    }
}
