package namidevelopment.kiriyaga.nami.impl.feature.combat;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PostTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;

import java.util.HashMap;
import java.util.Map;

import static namidevelopment.kiriyaga.nami.Nami.INVENTORY_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class ReplenishFeature extends Feature {

    public final IntSetting percentage = addSetting(new IntSetting("Percentage", 20, 10, 50));
    public final BoolSetting alternative = addSetting(new BoolSetting("Alternative", true));
    public final BoolSetting inScreen = addSetting(new BoolSetting("InScreen", false));

    private final Map<Integer, Integer> hotbarTicks = new HashMap<>();
    private final Map<Integer, Item> lastHotbarItems = new HashMap<>();

    public ReplenishFeature() {
        super("Replenish", "Automatically refills items in hotbar.", FeatureCategory.of("Combat"));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onTick(PostTickEvent event) {
        if (MC.level == null || MC.player == null) return;
        if (!inScreen.get() && MC.screen != null) return;
        LocalPlayer player = MC.player;
        ItemStack cursor = player.containerMenu.getCarried();

        if (!cursor.isEmpty()) return;

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = player.getInventory().getItem(hotbarSlot);
            Item currentItem = stack.isEmpty() ? null : stack.getItem();

            if (lastHotbarItems.getOrDefault(hotbarSlot, null) != currentItem) {
                hotbarTicks.put(hotbarSlot, 0);
                lastHotbarItems.put(hotbarSlot, currentItem);
            } else {
                hotbarTicks.put(hotbarSlot, hotbarTicks.getOrDefault(hotbarSlot, 0) + 1);
            }

            if (hotbarTicks.getOrDefault(hotbarSlot, 0) < 10) continue;

            if (stack.isEmpty()) continue;

            int maxCount = stack.getMaxStackSize();
            int minCount = Math.max(1, (int) (maxCount * (percentage.get() / 100f)));

            if (stack.getCount() < minCount) {
                int invSlot = findInventorySlotToReplenish(stack);
                if (invSlot != -1) {
                    swap(hotbarSlot, invSlot);
                    return;
                }
            }
        }
    }

    private int findInventorySlotToReplenish(ItemStack target) {
        LocalPlayer player = MC.player;

        for (int i = 9; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(stack, target)) continue; // this is component not nbt, but anyway it works the same since components are just wrapper for nbt
            return i;
        }
        return -1;
    }

    private void swap(int hotbarSlot, int invSlot) {
        int realInvSlot = invSlot;
        if (invSlot < 9) {
            realInvSlot += 36;
        }

        int realHotbarSlot = hotbarSlot + 36;

        //boolean inventoryOpen = MC.currentScreen instanceof InventoryScreen || MC.currentScreen instanceof HudEditorScreen || MC.currentScreen instanceof ClickGuiScreen;

        if (alternative.get()) {
            INVENTORY_SERVICE.getClickHandler().quickMoveSlot(realInvSlot);
        } else {
            INVENTORY_SERVICE.getClickHandler().pickupSlot(realInvSlot);
            INVENTORY_SERVICE.getClickHandler().pickupSlot(realHotbarSlot);
            INVENTORY_SERVICE.getClickHandler().pickupSlot(realInvSlot);
        }
    }
}