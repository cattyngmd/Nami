package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.DoubleSetting;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;
import namidevelopment.kiriyaga.api.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.Items;

import static namidevelopment.kiriyaga.api.NamiApi.MC;
import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.util.InteractionUtils.interactWithEntity;

@RegisterFeature
public class AutoCowFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3, 1.0, 5.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 1, 0, 20));
    public final BoolSetting swapBack = addSetting(new BoolSetting("SwapBack", true));
    public final BoolSetting multitask = addSetting(new BoolSetting("Multitask", false));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    private int cooldown = 0;

    public AutoCowFeature() {
        super("AutoCow", "Automatically milks nearby cows.", FeatureCategory.of("World"), "cow", "milk", "autocow");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.PASSIVE, 10, true)) {
            if (!(entity instanceof Cow cow)) continue;
            if (!cow.isAlive() || cow.isBaby()) continue;

            if (interactWithEntity(entity, Items.BUCKET, swapBack.get(), multitask.get(), range.get(), swing.get(), rotate.get(), this.name)) {
                cooldown = delay.get();
                break;
            }
        }
    }
}
