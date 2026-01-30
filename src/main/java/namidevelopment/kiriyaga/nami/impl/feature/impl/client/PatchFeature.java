package namidevelopment.kiriyaga.nami.impl.feature.impl.client;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.nami.event.impl.SprintResetEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.mixin.DuckBundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class PatchFeature extends Feature {

    public final BoolSetting grimAttackVelocity = addSetting(new BoolSetting("GrimAttackVelocity", true));
    public final BoolSetting preventUpdateSlot = addSetting(new BoolSetting("PreventUpdateSlot", true));
    public final BoolSetting slotDragDesync = addSetting(new BoolSetting("SlotDragDesync", true));
    public final BoolSetting silentSwapFix = addSetting(new BoolSetting("SilentSwapFix", true));

    public final AtomicBoolean b = new AtomicBoolean(false);

    public PatchFeature() {
        super("Patch", "Any kind of hotfixes you should apply based on what server and ac u on.", FeatureCategory.of("Client"));
        if (!this.isEnabled())
            this.toggle();
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSprintResetEvent(SprintResetEvent event) {
        if (grimAttackVelocity.get() && !event.isCancelled())
                event.cancel();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        Packet<?> p = event.getPacket();

        if (b.get() && silentSwapFix.get() && p instanceof ClientboundContainerSetSlotPacket packet) {
            if (MC.player != null) {
                if (packet.getContainerId() == 0) { // only player inventory, syncid of player inventory is always 0
                    int slot = packet.getSlot();

                    if (slot >= 0 && slot <= 8) { // onlu hotbar
                        ItemStack packetStack = packet.getItem();
                        ItemStack handStack = MC.player.getMainHandItem();

                        if (!packetStack.isEmpty() && !handStack.isEmpty()) {
                            if (ItemStack.isSameItem(packetStack, handStack) && packetStack.getCount() == handStack.getCount()) {
                                event.cancel(); // TODO: maybe delay it to 2 ticks instead of canceling, like in mio
                                return;
                            }
                        }
                    }
                }
            }
            b.set(false);
        }

        if (preventUpdateSlot.get() && p instanceof ClientboundBundlePacket packet) {
            List<Packet<?>> allowedBundle = new ArrayList<>();
            for (Packet<?> packet1 : packet.subPackets()) {
                if (packet1 instanceof ClientboundContainerSetSlotPacket)
                    continue;

                allowedBundle.add(packet1);
            }
            ((DuckBundlePacket) packet).setIterable(allowedBundle);
        }
    }
}
