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

import java.util.ArrayList;
import java.util.List;

@RegisterFeature
public class PatchFeature extends Feature {

    public final BoolSetting grimAttackVelocity = addSetting(new BoolSetting("GrimAttackVelocity", true));
    public final BoolSetting preventUpdateSlot = addSetting(new BoolSetting("PreventUpdateSlot", true));
    public final BoolSetting slotDragDesync = addSetting(new BoolSetting("SlotDragDesync", true));

    public PatchFeature() {
        super("Patch", "Any kind of hotfixes you defenetly should apply based on what server and ac u on.", FeatureCategory.of("Client"), "entity", "entitySERVICE", "enity");
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

    @SubscribeEvent
    public void onPacketInbound(PacketReceiveEvent event) {
        if (!preventUpdateSlot.get())
            return;

        if (event.getPacket() instanceof ClientboundBundlePacket packet) {
            List<Packet<?>> allowedBundle = new ArrayList<>();
            for (Packet<?> packet1 : packet.subPackets()) {
                if (packet1 instanceof ClientboundContainerSetSlotPacket) {
                    continue;
                }
                allowedBundle.add(packet1);
            }
            ((DuckBundlePacket) packet).setIterable(allowedBundle);
        }
    }
}
