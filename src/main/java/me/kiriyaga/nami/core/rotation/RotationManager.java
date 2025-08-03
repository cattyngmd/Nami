package me.kiriyaga.nami.core.rotation;

public class RotationManager {

    private final RotationStateHandler stateHandler = new RotationStateHandler();
    private final RotationRequestHandler requestHandler = new RotationRequestHandler(stateHandler);
    private final RotationTickHandler tickHandler = new RotationTickHandler(stateHandler, requestHandler);

    public void init() {
        tickHandler.init();
    }

    public RotationStateHandler getStateHandler() {
        return stateHandler;
    }

    public RotationRequestHandler getRequestHandler() {
        return requestHandler;
    }

    public RotationTickHandler getTickHandler() {
        return tickHandler;
    }
}
