package me.kiriyaga.nami.impl.feature.impl.combat;

import me.kiriyaga.nami.api.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.util.Timer;
import me.kiriyaga.nami.util.entity.TargetUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;

import static me.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AutoPotFeature extends Feature { // TODO: refactor this

    public enum SwapMode { NORMAL, SILENT }
    public enum ThrowMode { ABOVE, UNDER }

    private final EnumSetting<Pot> potEffect = addSetting(new EnumSetting<>("Effect", Pot.RESISTANCE));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));
    private final EnumSetting<ThrowMode> throwMode = addSetting(new EnumSetting<>("Throw", ThrowMode.UNDER));
    private final BoolSetting whenNoTarget = addSetting(new BoolSetting("NoTarget", false));
    private final BoolSetting onlyPhased = addSetting(new BoolSetting("OnlyPhased", false));
    private final EnumSetting<SwapMode> swapMode = addSetting(new EnumSetting<>("Swap", SwapMode.NORMAL));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));

    private final Timer throwTimer = new Timer();

    public AutoPotFeature() {
        super("AutoPot", "Throws specified splash potion under/above you.", FeatureCategory.of("Combat"), "autopot");
        throwMode.setShowCondition(rotate::get);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPreTick(PreTickEvent ev) {
        if (!isEnabled() || MC.player == null || MC.level == null) return;

        if (MC.player.hasEffect(potEffect.get().getEffect())) {
            if (selfToggle.get())
                toggle();
            return;
        }

        if (whenNoTarget.get() && TargetUtils.getTarget() != null) {
            if (selfToggle.get())
                toggle();
            return;
        }

        if (onlyPhased.get() && !isPhased()) {
            if (selfToggle.get())
                toggle();

            return;
        }

        int potSlot = getSlot(potEffect.get());
        int potInvSlot = getSlotInInventory(potEffect.get());
        int prev = MC.player.getInventory().getSelectedSlot();
        if (potSlot == -1) {
            if (potInvSlot != -1) {
                if (!throwTimer.hasElapsed(5000)) return;

                float pitch = 90.00f;

                switch (throwMode.get()) {
                    case ABOVE -> pitch = -90.00f;
                    case UNDER -> pitch = 90.00f;
                    default -> pitch = 90.00f;
                }

                if (rotate.get()) {
                    ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(
                            this.name,
                            6,
                            MC.player.getYRot(),
                            pitch,
                            RotationsFeature.RotationMode.MOTION
                    ));

                    if (!ROTATION_SERVICE.getRequestHandler().isCompleted(this.name)) return;
                }

                move(potInvSlot, prev);

                throwTimer.reset();

                switch (swapMode.get()) {
                    case NORMAL -> {
                        INVENTORY_SERVICE.getSlotHandler().attemptSwitch(potSlot);
                        MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
                    }
                    case SILENT -> {
                        INVENTORY_SERVICE.getSlotHandler().attemptSwitch(potSlot);
                        MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
                        INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prev);
                    }
                }

                move(potInvSlot, prev);
            } else {
                if (selfToggle.get())
                    toggle();
                return;
            }
        }

        if (!throwTimer.hasElapsed(5000)) return;

        float pitch = 90.00f;

        switch (throwMode.get()) {
            case ABOVE -> pitch = -90.00f;
            case UNDER -> pitch = 90.00f;
            default -> pitch = 90.00f;
        }

        if (rotate.get()) {
            ROTATION_SERVICE.getRequestHandler().submit(new RotationRequest(
                    this.name,
                    6,
                    MC.player.getYRot(),
                    pitch,
                    RotationsFeature.RotationMode.MOTION
            ));

            if (!ROTATION_SERVICE.getRequestHandler().isCompleted(this.name)) return;
        }

        int prevSlot = MC.player.getInventory().getSelectedSlot();
        throwTimer.reset();

        switch (swapMode.get()) {
            case NORMAL -> {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(potSlot);
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
            }
            case SILENT -> {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(potSlot);
                MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(prevSlot);
            }
        }
    }

    private int getSlot(Pot targetEffect) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != Items.SPLASH_POTION) continue;

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) continue;

            for (MobEffectInstance inst : contents.getAllEffects()) {
                if (inst.getEffect() == targetEffect.getEffect()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void move(int invSlot, int hotbarSlot) {
        int realInv = invSlot < 9 ? invSlot + 36 : invSlot;
        int realHotbar = hotbarSlot + 36;

        INVENTORY_SERVICE.getClickHandler().pickupSlot(realInv);
        INVENTORY_SERVICE.getClickHandler().pickupSlot(realHotbar);
        INVENTORY_SERVICE.getClickHandler().pickupSlot(realInv);
    }

    private int getSlotInInventory(AutoPotFeature.Pot targetEffect) {
        LocalPlayer player = MC.player;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != Items.SPLASH_POTION) continue;

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) continue;

            for (MobEffectInstance inst : contents.getAllEffects()) {
                if (inst.getEffect() == targetEffect.getEffect()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public enum Pot {
        STRENGTH(MobEffects.STRENGTH, Items.SPLASH_POTION),
        SPEED(MobEffects.SPEED, Items.SPLASH_POTION),
        JUMP_BOOST(MobEffects.JUMP_BOOST, Items.SPLASH_POTION),
        RESISTANCE(MobEffects.RESISTANCE, Items.SPLASH_POTION);

        private final Holder effect;
        private final Item item;

        Pot(Holder effect, Item item) {
            this.effect = effect;
            this.item = item;
        }

        public Holder getEffect() {
            return effect;
        }

        public Item getItem() {
            return item;
        }
    }

    private boolean isPhased() {
        LocalPlayer player = MC.player;
        if (player == null || MC.level == null) return false;

        AABB box = player.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.ceil(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = MC.level.getBlockState(pos).getCollisionShape(MC.level, pos);
                    if (!shape.isEmpty() && shape.bounds().move(pos).intersects(box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
