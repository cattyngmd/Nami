package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.SprintResetEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.mixin.DuckBundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

import java.util.ArrayList;
import java.util.List;

@RegisterModule
public class PatchModule extends Module {

    public final BoolSetting grimAttackVelocity = addSetting(new BoolSetting("GrimAttackVelocity", true));
    public final BoolSetting preventUpdateSlot = addSetting(new BoolSetting("PreventUpdateSlot", true));
    public final BoolSetting slotDragDesync = addSetting(new BoolSetting("SlotDragDesync", true));

    public PatchModule() {
        super("Patch", "Any kind of hotfixes you defenetly should apply based on what server and ac u on.", ModuleCategory.of("Client"), "entity", "entitymanager", "enity");
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
