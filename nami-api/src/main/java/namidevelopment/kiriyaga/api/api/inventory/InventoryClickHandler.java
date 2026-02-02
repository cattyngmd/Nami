package namidevelopment.kiriyaga.api.api.inventory;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.NonNullList;

import com.google.common.collect.Lists;

import java.util.List;

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

        if (API_MC.screen instanceof ShulkerBoxScreen
                || API_MC.screen instanceof AnvilScreen
                || API_MC.screen instanceof BrewingStandScreen
                || API_MC.screen instanceof CartographyTableScreen
                || API_MC.screen instanceof CrafterScreen
                || API_MC.screen instanceof EnchantmentScreen
                || API_MC.screen instanceof FurnaceScreen
                || API_MC.screen instanceof GrindstoneScreen
                || API_MC.screen instanceof HopperScreen
                || API_MC.screen instanceof HorseInventoryScreen
                || API_MC.screen instanceof MerchantScreen
                || API_MC.screen instanceof SmithingScreen
                || API_MC.screen instanceof SmokerScreen
                || API_MC.screen instanceof StonecutterScreen
                || (API_MC.screen instanceof ContainerScreen && !skipGeneric)
                || API_MC.screen instanceof CreativeModeInventoryScreen) {
            FEATURE_SERVICE.getStorage().getByClass(AutoTotemFeature.class).addDeathReason("invfail", "Inventory Fail");
            return false;
        }

        AbstractContainerMenu handler = API_MC.player.containerMenu;

        NonNullList<Slot> slots = handler.slots;
        List<ItemStack> before = Lists.newArrayListWithCapacity(slots.size());
        for (Slot s : slots) before.add(s.getItem().copy());

        API_MC.gameMode.handleInventoryMouseClick(handler.containerId, slot, button, type, API_MC.player);
        return true;
    }
}