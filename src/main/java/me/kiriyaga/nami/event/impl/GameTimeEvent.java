package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.world.entity.Entity;

public class GameTimeEvent extends Event {
    private float ticks;

    public GameTimeEvent(float ticks) {
        this.ticks = ticks;
    }

    public float getTicks(){
        return ticks;
    }

    public void setTicks(float ticks){
        this.ticks = ticks;
    }
}
