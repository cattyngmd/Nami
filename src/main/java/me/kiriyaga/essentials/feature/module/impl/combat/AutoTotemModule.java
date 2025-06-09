package me.kiriyaga.essentials.feature.module.impl.combat;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.event.impl.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

import static me.kiriyaga.essentials.Essentials.*;

public class AutoTotemModule extends Module {

    private final BoolSetting antiDesync = addSetting(new BoolSetting("Desync", true));
    private final BoolSetting packetPlace = addSetting(new BoolSetting("Instant", false));
    private final BoolSetting deathLog = addSetting(new BoolSetting("Log", false));

    private boolean pendingTotem = false;
    private long lastAttemptTime = 0;
    private boolean instantTriggered = false;
    private int totemCount = 0;

    public AutoTotemModule() {
        super("AutoTotem", "Insane shit.", Category.COMBAT);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdate(UpdateEvent event) {
        if (MINECRAFT.world == null || MINECRAFT.player == null) return;

        if (instantTriggered) {
            instantTriggered = false;
            return;
        }

        attemptPlaceTotem();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (packetPlace.get() && event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 35 && packet.getEntity(MINECRAFT.world) == MINECRAFT.player) {
                attemptPlaceTotem();
                instantTriggered = true;
            }
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getEntity(MINECRAFT.world) == MINECRAFT.player && packet.getStatus() == 3) {
                logDeathData();
            }
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
                MINECRAFT.interactionManager.clickSlot(syncId, invSlot, 40, SlotActionType.SWAP, player);
                lastAttemptTime = System.currentTimeMillis();
            }
        }

        totemCount = countTotems();
        setDisplayInfo(""+totemCount);

        if (antiDesync.get() && pendingTotem) {
            long now = System.currentTimeMillis();
            int ping = PING_MANAGER.getPing();
            int delay = Math.max(ping, 15);

            if (now - lastAttemptTime > delay) {
                int totemSlot = findTotemSlot();
                if (totemSlot != -1) {
                    int syncId = player.currentScreenHandler.syncId;
                    int invSlot = convertSlot(totemSlot);
                    MINECRAFT.interactionManager.clickSlot(syncId, invSlot, 40, SlotActionType.SWAP, player);
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
        boolean hasTotem = totemCount == 0;
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
        info.append("\n=== AutoTotem Death Log ===")
                .append("\nCause: ").append(reason)
                .append("\nPing: ").append(ping).append(" ms")
                .append("\nInstant: ").append(instantTriggered)
                .append("\nTotems Available: ").append(totemCount)
                .append("\nPending Totem: ").append(pendingTotem)
                .append("\nLast Swap Attempt: ").append(timeSinceLastSwap).append(" ms ago")
                .append("\n============================");

        CHAT_MANAGER.sendPersistent(AutoTotemModule.class.getName(), info.toString());
    }

}
