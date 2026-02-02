package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
import net.minecraft.network.protocol.Packet;

public class PacketReceiveEvent extends Event {
    public Packet<?> packet;

    public PacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }
}
