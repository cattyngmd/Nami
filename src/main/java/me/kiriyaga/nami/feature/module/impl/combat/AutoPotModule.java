package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoPotModule extends Module {

    public enum SwapMode { NORMAL, SILENT }
    public enum ThrowMode { ABOVE, UNDER }

    private final EnumSetting<Pot> potEffect = addSetting(new EnumSetting<>("Effect", Pot.RESISTANCE));
    private final EnumSetting<ThrowMode> throwMode = addSetting(new EnumSetting<>("Effect", ThrowMode.UNDER));
    private final BoolSetting whenNoTarget = addSetting(new BoolSetting("NoTarget", false));
    private final BoolSetting onlyPhased = addSetting(new BoolSetting("OnlyPhased", true));
    private final EnumSetting<SwapMode> swapMode = addSetting(new EnumSetting<>("Swap", SwapMode.NORMAL));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));

    public AutoPotModule() {
        super("AutoPot", "Throws specified splash potion under/above you.", ModuleCategory.of("Combat"), "autopot");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPreTick(PreTickEvent ev) {
        if (!isEnabled() || MC.player == null || MC.world == null) return;

        if (MC.player.hasStatusEffect(potEffect.get().getEffect())) {
            if (selfToggle.get())
                toggle();
            return;
        }

        if (whenNoTarget.get() && ENTITY_MANAGER.getTarget() != null) {
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
        if (potSlot == -1) {
            if (selfToggle.get())
                toggle();
            return;
        }

        float pitch = 90.00f;

        switch (throwMode.get()) {
            case ABOVE -> pitch = -90.00f;
            case UNDER -> pitch = 90.00f;
            default -> pitch = 90.00f;
        }

        ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                this.name,
                6,
                MC.player.getYaw(),
                pitch,
                RotationModule.RotationMode.MOTION
        ));

        if (!ROTATION_MANAGER.getRequestHandler().isCompleted(this.name)) return;

        int prevSlot = MC.player.getInventory().getSelectedSlot();

        switch (swapMode.get()) {
            case NORMAL -> {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(potSlot);
                MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            }
            case SILENT -> {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(potSlot);
                MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prevSlot);
            }
        }
    }

    private int getSlot(Pot targetEffect) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.isEmpty() || stack.getItem() != Items.SPLASH_POTION) continue;

            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) continue;

            for (StatusEffectInstance inst : contents.getEffects()) {
                if (inst.getEffectType() == targetEffect.getEffect()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public enum Pot {
        STRENGTH(StatusEffects.STRENGTH, Items.SPLASH_POTION),
        SPEED(StatusEffects.SPEED, Items.SPLASH_POTION),
        JUMP_BOOST(StatusEffects.JUMP_BOOST, Items.SPLASH_POTION),
        RESISTANCE(StatusEffects.RESISTANCE, Items.SPLASH_POTION);

        private final RegistryEntry effect;
        private final Item item;

        Pot(RegistryEntry effect, Item item) {
            this.effect = effect;
            this.item = item;
        }

        public RegistryEntry getEffect() {
            return effect;
        }

        public Item getItem() {
            return item;
        }
    }

    private boolean isPhased() {
        ClientPlayerEntity player = MC.player;
        if (player == null || MC.world == null) return false;

        Box box = player.getBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = MC.world.getBlockState(pos).getCollisionShape(MC.world, pos);
                    if (!shape.isEmpty() && shape.getBoundingBox().offset(pos).intersects(box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
