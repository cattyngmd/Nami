package namidevelopment.kiriyaga.nami.event.impl;

import namidevelopment.kiriyaga.nami.event.Event;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
