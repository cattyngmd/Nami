package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.EnchantmentUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoTotemModule extends Module {

    private enum Offhand { CRYSTAL, GAPPLE, ITEMFRAME, MENDING}

    private final BoolSetting offhandOverride = addSetting(new BoolSetting("override", false));
    private final IntSetting overrideHealth = addSetting(new IntSetting("health", 16, 10, 36));
    private final EnumSetting<Offhand> overrideItem = addSetting(new EnumSetting<>("item", Offhand.CRYSTAL));
    private final BoolSetting fastSwap = addSetting(new BoolSetting("alternative", false));
    private final BoolSetting fastSwapHotbar = addSetting(new BoolSetting("safety", false));
    private final IntSetting fastSlot = addSetting(new IntSetting("safety slot", 8, 0, 8));
    private final BoolSetting cursorStack = addSetting(new BoolSetting("cursor stack", true));
    private final BoolSetting deathLog = addSetting(new BoolSetting("log", false));

    private final Map<String, String> deathReasons = new ConcurrentHashMap<>();

    private boolean pendingTotem = false;
    private long lastAttemptTime = 0;
    private int totemCount = 0;

    public AutoTotemModule() {
        super("auto totem", "Automatically places totem in your hand.", ModuleCategory.of("combat"), "autototem");
        fastSwapHotbar.setShowCondition(() -> fastSwap.get());
        fastSlot.setShowCondition(() -> fastSwapHotbar.get());
        fastSlot.setShowCondition(() -> (fastSwapHotbar.get()&&fastSwap.get()));
        cursorStack.setShowCondition(() -> !(fastSwap.get()));

        overrideHealth.setShowCondition(offhandOverride::get);
        overrideItem.setShowCondition(offhandOverride::get);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPostTick(PostTickEvent event) {
        if (MC.world == null || MC.player == null) return;

        int totemCount = 0;
        for (ItemStack stack : MC.player.getInventory().getMainStacks()) {
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totemCount += stack.getCount();
            }
        }
        ItemStack offHandStack = MC.player.getOffHandStack();
        if (offHandStack.getItem() == Items.TOTEM_OF_UNDYING) {
            totemCount += offHandStack.getCount();
        }
        this.setDisplayInfo(String.valueOf(totemCount));

        attemptPlaceOffhand();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onReceivePacket(PacketReceiveEvent event) {
        if (MC.world == null || MC.player == null) return;

        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getEntity(MC.world) == MC.player && packet.getStatus() == 3 && deathLog.get()) {
                MC.execute(this::logDeathData);
            }
        }
    }

    private void attemptPlaceOffhand() {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        ItemStack offhandStack = player.getOffHandStack();
        ItemStack targetStack = null;
        boolean overrideActive = false;

        if (offhandOverride.get()) {
            int effectiveHealth = (int) (player.getHealth() + player.getAbsorptionAmount());
            if (effectiveHealth >= overrideHealth.get()) {
                targetStack = getOverrideStack();
                if (targetStack != null) {
                    overrideActive = true;
                }
            }
        }

        if (targetStack == null) {
            targetStack = findTotemStack();
            if (targetStack == null) return;
        }

        if (offhandStack.getItem() == targetStack.getItem()) return;

        int targetSlot = findInventorySlot(targetStack);
        if (targetSlot == -1) return;

        if (fastSwap.get()) {
            int fastSlotIndex = fastSlot.get();
            if (fastSlotIndex >= 0 && fastSlotIndex <= 8) {
                ItemStack fastSlotStack = player.getInventory().getStack(fastSlotIndex);
                boolean fastSlotHasTarget = fastSlotStack.getItem() == targetStack.getItem();

                if (fastSwapHotbar.get()) {
                    if (!fastSlotHasTarget && targetSlot != fastSlotIndex) {
                        INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(targetSlot), fastSlotIndex);
                        lastAttemptTime = System.currentTimeMillis();
                        fastSlotHasTarget = true;
                    }
                    offhandStack = player.getOffHandStack();
                    if (offhandStack.getItem() != targetStack.getItem() && fastSlotHasTarget) {
                        INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(fastSlotIndex), 40);
                        lastAttemptTime = System.currentTimeMillis();
                    }
                } else {
                    if (offhandStack.getItem() != targetStack.getItem()) {
                        INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(targetSlot), 40);
                        lastAttemptTime = System.currentTimeMillis();
                    }
                }
            }
        } else {
            swapToOffhand(targetSlot);
            lastAttemptTime = System.currentTimeMillis();
        }

        totemCount = countTotems();
        setDisplayInfo("" + totemCount);
    }

    private ItemStack findTotemStack() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) return stack;
        }
        return null;
    }

    private ItemStack getOverrideStack() {
        Offhand type = overrideItem.get();
        ClientPlayerEntity player = MC.player;

        switch (type) {
            case CRYSTAL:
                return new ItemStack(Items.END_CRYSTAL);
            case GAPPLE:
                return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
            case ITEMFRAME:
                return new ItemStack(Items.ITEM_FRAME);
            case MENDING:
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (stack.isEmpty()) continue;

                    if (stack.isIn(ItemTags.HEAD_ARMOR)) continue;
                    if (stack.isIn(ItemTags.CHEST_ARMOR)) continue;
                    if (stack.isIn(ItemTags.LEG_ARMOR)) continue;
                    if (stack.isIn(ItemTags.FOOT_ARMOR)) continue;

                    if (!hasMending(stack)) continue;
                    if (isFullyRepaired(stack)) continue;

                    return stack;
                }
                break;
        }
        return null;
    }

    private void swapToOffhand(int invSlot) {
        int realSlot = convertSlot(invSlot);

        if (cursorStack.get()) {
            ItemStack cursor = MC.player.currentScreenHandler.getCursorStack();

            if (cursor.isEmpty()) {
                INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
                cursor = MC.player.currentScreenHandler.getCursorStack();
            }

            if (!cursor.isEmpty()) {
                INVENTORY_MANAGER.getClickHandler().pickupSlot(45);
            }
        } else {
            INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
            INVENTORY_MANAGER.getClickHandler().pickupSlot(45);
        }
    }

    private int findTotemSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private int countTotems() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    public void addDeathReason(String key, String reasonDescription) {
        deathReasons.put(key, reasonDescription);
    }

    public void removeDeathReason(String key) {
        deathReasons.remove(key);
    }

    public void clearDeathReasons() {
        deathReasons.clear();
    }

    private void logDeathData() {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        int ping = PING_MANAGER.getPing();
        boolean hasTotem = totemCount > 0;
        long timeSinceLastSwap = System.currentTimeMillis() - lastAttemptTime;

        if (!hasTotem) {
            addDeathReason("notots", "NO_TOTEMS");
        } else {
            removeDeathReason("notots");
        }

        if (ping > 125) {
            addDeathReason("highping", "HIGH_PING " + ping + " ms");
        } else {
            removeDeathReason("highping");
        }

        if (deathReasons.isEmpty()) {
            addDeathReason("unknown", "UNKNOWN_CAUSE");
        } else {
            removeDeathReason("unknown");
        }

        StringBuilder reasonsBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : deathReasons.entrySet()) {
            reasonsBuilder.append("- ").append(entry.getValue()).append("\n");
        }

        Text message = CAT_FORMAT.format(
                "\n=== {g}AutoTotem{reset} ===\n" +
                        "Death reasons:\n{g}" + reasonsBuilder.toString() + "{reset}\n" +
                        "Ping: {g}" + ping + " ms{reset}\n" +
                        "Totems Available: {g}" + totemCount + "{reset}\n" +
                        "Pending Totem: {g}" + pendingTotem + "{reset}\n" +
                        "Last Swap Attempt: {g}" + timeSinceLastSwap + " ms ago{reset}\n" +
                        "============================"
        );

        CHAT_MANAGER.sendPersistent(AutoTotemModule.class.getName(), message);
    }

    private int findInventorySlot(ItemStack stack) {
        for (int i = 0; i < 36; i++) {
            if (MC.player.getInventory().getStack(i).getItem() == stack.getItem()) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasMending(ItemStack stack) {
        return EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.MENDING) > 0;
    }

    private boolean isFullyRepaired(ItemStack stack) {
        if (!stack.isDamageable()) return true;
        return stack.getDamage() == 0;
    }
}