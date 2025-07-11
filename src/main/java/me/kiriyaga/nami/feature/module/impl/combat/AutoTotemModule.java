package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

import static me.kiriyaga.nami.Nami.*;

public class AutoTotemModule extends Module {

    private final BoolSetting fastSwap = addSetting(new BoolSetting("fast swap", false));
    private final IntSetting totemSlotSetting = addSetting(new IntSetting("totem slot", 9, 1, 9));
    private final BoolSetting antiDesync = addSetting(new BoolSetting("desync check", false));
    private final BoolSetting deathLog = addSetting(new BoolSetting("log", false));
    private boolean pendingTotem = false;
    private long lastAttemptTime = 0;
    private int totemCount = 0;

    public AutoTotemModule() {
        super("auto totem", "Automatically places totem in your hand.", Category.combat, "autototem");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPostTick(PostTickEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null) return;

        if (fastSwap.get()) {
            attemptFastSwap();
        } else if (MINECRAFT.currentScreen == null || MINECRAFT.currentScreen instanceof InventoryScreen) {
            attemptPlaceTotem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onReceivePacket(PacketReceiveEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null)
            return;

        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getEntity(MINECRAFT.world) == MINECRAFT.player && packet.getStatus() == 3 && deathLog.get()) {
                MINECRAFT.execute(this::logDeathData);
            }
        }
    }

    private void attemptFastSwap() {
        ClientPlayerEntity player = MINECRAFT.player;
        if (player == null) return;

        int hotbarSlot = totemSlotSetting.get() - 1;
        int syncId = player.currentScreenHandler.syncId;

        ItemStack hotbarStack = player.getInventory().getStack(hotbarSlot);
        ItemStack offhand = player.getOffHandStack();

        if (hotbarStack.getItem() != Items.TOTEM_OF_UNDYING) {
            int totemSlot = findTotemSlot();
            if (totemSlot == -1) return;

            int inventorySlot = convertSlot(totemSlot);
            MINECRAFT.interactionManager.clickSlot(syncId, inventorySlot, hotbarSlot, SlotActionType.SWAP, player);
            return; // we do not want to one-tick swap methods
        }

        if (offhand.getItem() != Items.TOTEM_OF_UNDYING) {
            MINECRAFT.interactionManager.clickSlot(syncId, 45, hotbarSlot, SlotActionType.SWAP, player);
        }
    }


    private void attemptPlaceTotem() {
        ClientPlayerEntity player = MINECRAFT.player;
        ItemStack offhandStack = player.getOffHandStack();

        pendingTotem = offhandStack.getItem() != Items.TOTEM_OF_UNDYING;

        if (pendingTotem) {
            int totemSlot = findTotemSlot();
            if (totemSlot != -1) {
                int syncId = player.currentScreenHandler.syncId;
                int invSlot = convertSlot(totemSlot);

                MINECRAFT.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, player);
                MINECRAFT.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, player);

                lastAttemptTime = System.currentTimeMillis();
            }
        }

        totemCount = countTotems();
        setDisplayInfo("" + totemCount);

        if (antiDesync.get() && pendingTotem) {
            long now = System.currentTimeMillis();
            int ping = PING_MANAGER.getPing();
            int delay = Math.max(ping, 15);

            if (now - lastAttemptTime > delay) {
                int totemSlot = findTotemSlot();
                if (totemSlot != -1) {
                    int syncId = player.currentScreenHandler.syncId;
                    int invSlot = convertSlot(totemSlot);

                    MINECRAFT.interactionManager.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, player);
                    MINECRAFT.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, player);

                    lastAttemptTime = now;
                }
            }
        }
    }

    private int findTotemSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MINECRAFT.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private int countTotems() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MINECRAFT.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    private void logDeathData() {
        ClientPlayerEntity player = MINECRAFT.player;
        if (player == null) return;

        int ping = PING_MANAGER.getPing();
        boolean hasTotem = totemCount > 0;
        long timeSinceLastSwap = System.currentTimeMillis() - lastAttemptTime;

        StringBuilder reason = new StringBuilder();

        if (!hasTotem) {
            reason.append("NO_TOTEMS");
        } else if (ping > 125) {
            reason.append("HIGH_PING");
        } else {
            reason.append("UNKNOWN_CAUSE");
        }

        StringBuilder info = new StringBuilder();
        info.append("\n§c=== AutoTotem ===")
                .append("\nCause: ").append(reason)
                .append("\nPing: ").append(ping).append(" ms")
                .append("\nTotems Available: ").append(totemCount)
                .append("\nPending Totem: ").append(pendingTotem)
                .append("\nLast Swap Attempt: ").append(timeSinceLastSwap).append(" ms ago")
                .append("\n§c============================");

        CHAT_MANAGER.sendPersistent(AutoTotemModule.class.getName(), info.toString());
    }
}
