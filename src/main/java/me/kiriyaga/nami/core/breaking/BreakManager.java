package me.kiriyaga.nami.core.breaking;

public class BreakManager {
    private final BreakStateHandler stateHandler = new BreakStateHandler();
    private final BreakRequestHandler requestHandler = new BreakRequestHandler(stateHandler);
    private final BreakTickHandler tickHandler = new BreakTickHandler(stateHandler, requestHandler);

    public void init() {
        tickHandler.init();
    }

    public BreakStateHandler getStateHandler() {
        return stateHandler;
    }

    public BreakRequestHandler getRequestHandler() {
        return requestHandler;
    }

    public BreakTickHandler getTickHandler() {
        return tickHandler;
    }

    public boolean isBreaking() {
        return tickHandler.isBreaking();
    }
}
