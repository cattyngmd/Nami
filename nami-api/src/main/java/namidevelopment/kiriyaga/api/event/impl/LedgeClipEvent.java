package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;

public class LedgeClipEvent extends Event {
    private boolean clipped;

    public LedgeClipEvent() {
    }

    public boolean getClipped(){
        return clipped;
    }
    public void setClipped(boolean clipped){
        this.clipped = clipped;
    }
}
