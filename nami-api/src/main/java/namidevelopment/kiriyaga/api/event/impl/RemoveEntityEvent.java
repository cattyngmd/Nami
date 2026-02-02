package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

public class RemoveEntityEvent extends Event {

    private final ClientboundRemoveEntitiesPacket packet;

    public RemoveEntityEvent(ClientboundRemoveEntitiesPacket packet) {
        this.packet = packet;
    }

    public ClientboundRemoveEntitiesPacket getPacket() {
        return packet;
    }
}
