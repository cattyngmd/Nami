package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.gui.screen.ClickGuiScreen;
import me.kiriyaga.nami.feature.gui.screen.HudEditorScreen;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoTotemModule extends Module {

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

        attemptPlaceTotem();
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

    private void attemptPlaceTotem() {
        ClientPlayerEntity player = MC.player;
        ItemStack offhandStack = player.getOffHandStack();

        boolean hasOffhandTotem = offhandStack.getItem() == Items.TOTEM_OF_UNDYING;

        if (fastSwap.get()) {
            int fastSlotIndex = fastSlot.get();
            if (fastSlotIndex < 0 || fastSlotIndex > 8) return;

            int totemSlot = findTotemSlot();

            if (totemSlot == -1) {
                return;
            }

            ItemStack fastSlotStack = player.getInventory().getStack(fastSlotIndex);
            boolean fastSlotHasTotem = fastSlotStack.getItem() == Items.TOTEM_OF_UNDYING;

            if (fastSwapHotbar.get()) {
                if (!fastSlotHasTotem && totemSlot != fastSlotIndex) {
                    INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(totemSlot), fastSlotIndex);
                    lastAttemptTime = System.currentTimeMillis();
                    fastSlotHasTotem = true;
                }
                if (!hasOffhandTotem && fastSlotHasTotem) {
                    INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(fastSlotIndex), 40);

                    lastAttemptTime = System.currentTimeMillis();
                }
            } else {
                if (!hasOffhandTotem) {
                    INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(totemSlot), 40);
                    lastAttemptTime = System.currentTimeMillis();
                }
            }
        } else {
            int totemSlot = findTotemSlot();
            if (!hasOffhandTotem && totemSlot != -1) {
                swapToOffhand(totemSlot);
                lastAttemptTime = System.currentTimeMillis();
            }
        }

        totemCount = countTotems();
        setDisplayInfo("" + totemCount);
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
}