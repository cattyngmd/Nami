package me.kiriyaga.nami.impl.feature.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import me.kiriyaga.nami.impl.setting.impl.KeyBindSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.RotationUtils.getLookVectorFromYawPitch;

@RegisterFeature
public class ClickActionFeature extends Feature {

    public enum GroundAction { PEARL, WIND, EXP, NONE }

    public enum GlidingAction  { NONE, FIREWORK, WIND}

    private final EnumSetting<GroundAction> groundAction = addSetting(new EnumSetting<>("Ground", GroundAction.NONE));
    private final EnumSetting<GlidingAction> glidingAction = addSetting(new EnumSetting<>("Gliding", GlidingAction.FIREWORK));
    private final BoolSetting checkCooldown = addSetting(new BoolSetting("CheckCooldown", true));
    private final BoolSetting entityCheck = addSetting(new BoolSetting("EntityCheck", true));
    private final KeyBindSetting useKey = addSetting(new KeyBindSetting("Use", KeyBindSetting.KEY_NONE));

    public ClickActionFeature() {
        super("ClickAction", "Uses configured item when pressing key.", FeatureCategory.of("Combat"), "clickpearl");
    }

    private boolean recall;

    @Override
    public void onEnable() {
        useKey.setWasPressedLastTick(false);
        recall = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onTick(PreTickEvent ev) {
        if (MC.level == null || MC.player == null) return;

        boolean pressed = useKey.isPressed();
        if (groundAction.get() == GroundAction.EXP && pressed && !MC.player.isFallFlying()) {
            use();
            useKey.setWasPressedLastTick(pressed);
            return;
        }

        if (pressed && !useKey.wasPressedLastTick() || recall) {
            recall = false;
            if (MC.player.isFallFlying()) {
                useGlide();
            } else {
                use();
            }
        }

        useKey.setWasPressedLastTick(pressed);
    }

    private void use() {
        Item item = switch (groundAction.get()) {
            case PEARL -> Items.ENDER_PEARL;
            case WIND -> Items.WIND_CHARGE;
            case EXP -> Items.EXPERIENCE_BOTTLE;
            case NONE -> null;
        };

        if (item == null) return;

        if (checkCooldown.get() && MC.player.getCooldowns().isOnCooldown(item.getDefaultInstance())) {
            return;
        }

        if (entityCheck.get() && !canCastRay()) {
            return;
        }

        useItem(item);
    }

    private void useGlide() {
        Item item = switch (glidingAction.get()) {
            case FIREWORK -> Items.FIREWORK_ROCKET;
            case WIND -> Items.WIND_CHARGE;
            case NONE -> null;
        };

        if (item == null) return;

        if (checkCooldown.get() && MC.player.getCooldowns().isOnCooldown(item.getDefaultInstance())) {
            return;
        }

        useItem(item);
    }

    private void useItem(Item item) {
        int hotbarSlot = getSlotInHotbar(item);

        if (hotbarSlot != -1) {
            int prevSlot = MC.player.getInventory().getSelectedSlot();
            INVENTORY_SERVICE.getSlotHandler().attemptSwitch(hotbarSlot);
            MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
            INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prevSlot);
            return;
        }

       // NoSlowFeature noSlow = FEATURE_SERVICE.getStorage().getByClass(NoSlowFeature.class);

        int invSlot = getSlotInInventory(item);
        if (invSlot != -1) {
            int selectedHotbarIndex = MC.player.getInventory().getSelectedSlot(); // 0–8
            int containerInvSlot = convertSlot(invSlot);

            if (INVENTORY_SERVICE.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex)) {
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);

                INVENTORY_SERVICE.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);
            } else recall = true;
        }
    }

    private boolean canCastRay() {
        double rayRange = 6.0;

        for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity == MC.player) continue;
            if (MC.player.distanceToSqr(entity) > 100) continue;

            EntityHitResult hitResult = raycastTarget(MC.player, entity, rayRange,
                    ROTATION_SERVICE.getStateHandler().getServerYaw(),
                    ROTATION_SERVICE.getStateHandler().getServerPitch());

            if (hitResult != null)
                return false;
        }
        return true;
    }

    private int getSlotInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int getSlotInInventory(Item item) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    private EntityHitResult raycastTarget(Entity player, Entity target, double reach, float yaw, float pitch) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 look = getLookVectorFromYawPitch(yaw, pitch);
        Vec3 reachEnd = eyePos.add(look.scale(reach));

        AABB targetBox = target.getBoundingBox();

        if (targetBox.clip(eyePos, reachEnd).isPresent()) {
            return new EntityHitResult(target);
        }

        return null;
    }
}