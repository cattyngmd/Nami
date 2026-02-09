package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
import net.minecraft.world.entity.player.Player;

public class TotemPopEvent extends Event {

    private final int entityId;
    private final String name;
    private final Player player;
    private final int pops;

    public TotemPopEvent(int entityId, String name, Player player, int pops) {
        this.entityId = entityId;
        this.name = name;
        this.player = player;
        this.pops = pops;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public Player getPlayer() {
        return player;
    }

    public int getPops() {
        return pops;
    }
}
