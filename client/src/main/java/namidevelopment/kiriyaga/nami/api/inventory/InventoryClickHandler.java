package namidevelopment.kiriyaga.nami.api.inventory;

import namidevelopment.kiriyaga.nami.impl.feature.impl.combat.AutoTotemFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.movement.NoSlowFeature;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;

import com.google.common.collect.Lists;

import java.util.List;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class InventoryClickHandler {

    public boolean pickupSlot(int slotIndex, boolean skipGeneric) {
        return click(slotIndex, 0, ClickType.PICKUP, skipGeneric);
    }

    public boolean quickMoveSlot(int slotIndex, boolean skipGeneric) {
        return click(slotIndex, 0, ClickType.QUICK_MOVE, skipGeneric);
    }

    public boolean throwSlot(int slotIndex, boolean skipGeneric) {
        return click(slotIndex, 0, ClickType.THROW, skipGeneric);
    }

    public boolean swapSlot(int targetSlot, int hotbarSlotIndex, boolean skipGeneric) {
        return click(targetSlot, hotbarSlotIndex, ClickType.SWAP, skipGeneric);
    }


    public boolean pickupSlot(int slotIndex) {
        return click(slotIndex, 0, ClickType.PICKUP);
    }

    public boolean quickMoveSlot(int slotIndex) {
        return click(slotIndex, 0, ClickType.QUICK_MOVE);
    }

    public boolean throwSlot(int slotIndex) {
        return click(slotIndex, 0, ClickType.THROW);
    }

    public boolean swapSlot(int targetSlot, int hotbarSlotIndex) {
        return click(targetSlot, hotbarSlotIndex, ClickType.SWAP);
    }

    private boolean click(int slot, int button, ClickType type) {
        return click(slot, button, type, false);
    }

    private boolean click(int slot, int button, ClickType type, boolean skipGeneric) {
        if (slot < 0) return false;

        NoSlowFeature noSlow = FEATURE_SERVICE.getStorage().getByClass(NoSlowFeature.class);

        if (noSlow != null && noSlow.isEnabled()){
            if (noSlow.invMove.get() == NoSlowFeature.InvMove.WAIT && INPUT_SERVICE.hasAnyInput()) {
                FEATURE_SERVICE.getStorage().getByClass(AutoTotemFeature.class).addDeathReason("invmove", "Inventory Move not allowed by configuration");
                return false;
            }

            if (noSlow.invMove.get() == NoSlowFeature.InvMove.STOP) {
                if (INPUT_SERVICE.getFrozenTicks() == 2) return false; // if one stop call existed this tick, we cannot use any other click actions, since it will apply net tick only

                if (!INPUT_SERVICE.isFrozen() && INPUT_SERVICE.hasAnyInput()) {
                    INPUT_SERVICE.freezeInput(2);
                    return false;
                }
            }
        }

        if (MC.screen instanceof ShulkerBoxScreen
                || MC.screen instanceof AnvilScreen
                || MC.screen instanceof BrewingStandScreen
                || MC.screen instanceof CartographyTableScreen
                || MC.screen instanceof CrafterScreen
                || MC.screen instanceof EnchantmentScreen
                || MC.screen instanceof FurnaceScreen
                || MC.screen instanceof GrindstoneScreen
                || MC.screen instanceof HopperScreen
                || MC.screen instanceof HorseInventoryScreen
                || MC.screen instanceof MerchantScreen
                || MC.screen instanceof SmithingScreen
                || MC.screen instanceof SmokerScreen
                || MC.screen instanceof StonecutterScreen
                || (MC.screen instanceof ContainerScreen && !skipGeneric)
                || MC.screen instanceof CreativeModeInventoryScreen) {
            FEATURE_SERVICE.getStorage().getByClass(AutoTotemFeature.class).addDeathReason("invfail", "Inventory Fail");
            return false;
        }

        AbstractContainerMenu handler = MC.player.containerMenu;

        NonNullList<Slot> slots = handler.slots;
        List<ItemStack> before = Lists.newArrayListWithCapacity(slots.size());
        for (Slot s : slots) before.add(s.getItem().copy());

        MC.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, type, MC.player);
        return true;
    }
}