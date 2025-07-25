package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.gui.screen.ClickGuiScreen;
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

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoTotemModule extends Module {

    private final BoolSetting deathLog = addSetting(new BoolSetting("log", false));
    private final BoolSetting fastSwap = addSetting(new BoolSetting("fast swap", false));
    private final IntSetting fastSlot = addSetting(new IntSetting("swap slot", 8, 0, 8));

    private boolean pendingTotem = false;
    private long lastAttemptTime = 0;
    private int totemCount = 0;

    public AutoTotemModule() {
        super("auto totem", "Automatically places totem in your hand.", ModuleCategory.of("combat"), "autototem");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPostTick(PostTickEvent event) {
        if (MC.world == null || MC.player == null) return;

        if (MC.currentScreen == null || MC.currentScreen instanceof InventoryScreen || MC.currentScreen instanceof ChatScreen || MC.currentScreen instanceof ClickGuiScreen) {
            attemptPlaceTotem();
        }
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
        int totemSlot = findTotemSlot();

        if (fastSwap.get()) {
            int slot = fastSlot.get();
            if (slot < 0 || slot > 8) return;

            ItemStack fastSlotStack = player.getInventory().getStack(slot);
            boolean fastSlotHasTotem = fastSlotStack.getItem() == Items.TOTEM_OF_UNDYING;

            if (!fastSlotHasTotem && totemSlot != -1) {
                INVENTORY_MANAGER.getClickHandler().swapSlot(convertSlot(totemSlot), slot);
                lastAttemptTime = System.currentTimeMillis();
            } else if (fastSlotHasTotem && !hasOffhandTotem) {
                INVENTORY_MANAGER.getClickHandler().pickupSlot(convertSlot(slot));
                INVENTORY_MANAGER.getClickHandler().pickupSlot(45);
                lastAttemptTime = System.currentTimeMillis();
            }
        } else {
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
        INVENTORY_MANAGER.getClickHandler().pickupSlot(realSlot);
        INVENTORY_MANAGER.getClickHandler().pickupSlot(45);
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

    private void logDeathData() {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        int ping = PING_MANAGER.getPing();
        boolean hasTotem = totemCount > 0;
        long timeSinceLastSwap = System.currentTimeMillis() - lastAttemptTime;

        String reason;
        if (!hasTotem) {
            reason = "NO_TOTEMS";
        } else if (ping > 125) {
            reason = "HIGH_PING";
        } else {
            reason = "UNKNOWN_CAUSE";
        }

        Text message = CAT_FORMAT.format(
                "\n{primary}=== AutoTotem ===\n" +
                        "{primary}Cause: {global}" + reason + "{reset}\n" +
                        "{primary}Ping: {global}" + ping + " ms{reset}\n" +
                        "{primary}Totems Available: {global}" + totemCount + "{reset}\n" +
                        "{primary}Pending Totem: {global}" + pendingTotem + "{reset}\n" +
                        "{primary}Last Swap Attempt: {global}" + timeSinceLastSwap + " ms ago{reset}\n" +
                        "{primary}============================"
        );

        CHAT_MANAGER.sendPersistent(AutoTotemModule.class.getName(), message);
    }
}