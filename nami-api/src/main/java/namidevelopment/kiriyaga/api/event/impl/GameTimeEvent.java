package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;

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
