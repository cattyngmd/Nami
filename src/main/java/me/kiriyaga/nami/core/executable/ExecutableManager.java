package me.kiriyaga.nami.core.executable;

import static me.kiriyaga.nami.Nami.LOGGER;

public class ExecutableManager {

    private final ExecutableStateHandler stateHandler = new ExecutableStateHandler();
    private final ExecutableRequestHandler requestHandler = new ExecutableRequestHandler(stateHandler);
    private final ExecutableTickHandler tickHandler = new ExecutableTickHandler(stateHandler, requestHandler);

    public void init() {
        tickHandler.init();
        LOGGER.info("Executable Manager loaded.");
    }

    public ExecutableStateHandler getStateHandler() {
        return stateHandler;
    }

    public ExecutableRequestHandler getRequestHandler() {
        return requestHandler;
    }

    public ExecutableTickHandler getTickHandler() {
        return tickHandler;
    }
}
