package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;

import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class PacketSnifferModule extends Module {

    public final BoolSetting send = addSetting(new BoolSetting("send", false));
    public final BoolSetting receive = addSetting(new BoolSetting("receive", false));
    public final BoolSetting filterCommonPackets = addSetting(new BoolSetting("filter common", true));

    private final Set<String> filteredPackets = Set.of(
            "serverbound/minecraft:client_tick_end",
            "clientbound/minecraft:block_update",
            "clientbound/minecraft:set_entity_motion",
            "clientbound/minecraft:move_entity_pos",
            "clientbound/minecraft:move_entity_pos_rot",
            "clientbound/minecraft:rotate_head",
            "clientbound/minecraft:entity_position_sync"
    );

    public PacketSnifferModule() {
        super("packet sniffer", ".", ModuleCategory.of("client"), "packetsniffer");
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent ev) {
        if (!receive.get())
            return;

        String packetType = String.valueOf(ev.getPacket().getPacketType());

        if (filterCommonPackets.get() && filteredPackets.contains(packetType))
            return;

        CHAT_MANAGER.sendRaw("Receive packet: " + packetType);
    }

    @SubscribeEvent
    private void onPacketsend(PacketSendEvent ev) {
        if (!send.get())
            return;

        String packetType = String.valueOf(ev.getPacket().getPacketType());

        if (filterCommonPackets.get() && filteredPackets.contains(packetType))
            return;

        CHAT_MANAGER.sendRaw("Send packet: " + packetType);
    }

}
