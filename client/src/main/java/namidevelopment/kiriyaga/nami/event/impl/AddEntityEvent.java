package namidevelopment.kiriyaga.nami.event.impl;

import namidevelopment.kiriyaga.nami.event.Event;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;

public class AddEntityEvent extends Event {

    private final ClientboundAddEntityPacket packet;

    public AddEntityEvent(ClientboundAddEntityPacket packet) {
        this.packet = packet;
    }

    public ClientboundAddEntityPacket getPacket() {
        return packet;
    }
}
