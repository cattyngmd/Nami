package me.kiriyaga.nami.core.rotation;

/**
 * Rotation manager doc.
 * <p>
 * Manages motion rotations:
 * <ul>
 *     <li>{@link RotationStateHandler} — Stores current state of rotations, read docs;</li>
 *     <li>{@link RotationRequestHandler} — as-is request handler;</li>
 *     <li>{@link RotationTickHandler} — rotation intorpolation, movement fix.</li>
 * </ul>
 */
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
