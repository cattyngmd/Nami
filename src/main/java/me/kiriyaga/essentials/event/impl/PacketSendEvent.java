package me.kiriyaga.essentials.event.impl;

import me.kiriyaga.essentials.event.Event;
import net.minecraft.network.packet.Packet;

public class PacketSendEvent extends Event {
    private final Packet<?> packet;

    public PacketSendEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }
}
