package namidevelopment.kiriyaga.nami.impl.feature.impl.miscellaneous;

import namidevelopment.kiriyaga.nami.api.rotation.model.RotationRequest;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.RotationUtils.*;
import static namidevelopment.kiriyaga.nami.util.entity.HostileUtils.isAggressiveNow;

@RegisterFeature
public class AgroFeature extends Feature {

    public enum Mode {
        ENDERMAN,
        CREAKING
    }

    private final EnumSetting<Mode> modeSetting = new EnumSetting<>("Mode", Mode.ENDERMAN);

    public AgroFeature() {
        super("Agro", "Automatically looks at certain mobs.", FeatureCategory.of("Miscellaneous"));
        addSetting(modeSetting);
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;
        if (MC.player.isCreative() || MC.player.isFallFlying()) return;

        ItemStack helmet = MC.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() == Items.CARVED_PUMPKIN) return;

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : EntityUtils.getEntities(EntityUtils.EntityTypeCategory.ALL)) {
            if (entity == MC.player) continue;

            if (modeSetting.get() == Mode.ENDERMAN && !(entity instanceof EnderMan && !isAggressiveNow(entity))) continue;
            if (modeSetting.get() == Mode.CREAKING && !(entity instanceof Creaking creak && creak.isActive())) continue;

            double distance = MC.player.distanceToSqr(entity);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        if (closest != null) {
            Vec3 eyes = closest.getEyePosition();

            ROTATION_SERVICE.getRequestHandler().submit(
                    new RotationRequest(
                            AgroFeature.class.getName(),
                            2,
                            (float) getYawToVec(MC.player, eyes),
                            (float) getPitchToVec(MC.player, eyes),
                            RotationsFeature.RotationMode.MOTION
                    )
            );
        }
    }

    public EnumSetting<Mode> getModeSetting() {
        return modeSetting;
    }
}
