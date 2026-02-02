package namidevelopment.kiriyaga.api.core.executable;

import namidevelopment.kiriyaga.api.core.executable.model.ExecutableRequest;
import namidevelopment.kiriyaga.api.core.executable.model.ExecutableThreadType;
import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.event.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PostTickEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.event.impl.Render2DEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class ExecutableTickHandler {

    private final ExecutableStateHandler stateHandler;
    @SuppressWarnings("FieldCanBeLocal")
    private final ExecutableRequestHandler requestHandler;

    private ExecutorService asyncExecutor;

    public ExecutableTickHandler(ExecutableStateHandler stateHandler, ExecutableRequestHandler requestHandler) {
        this.stateHandler = stateHandler;
        this.requestHandler = requestHandler;
    }

    public void init() {
        EVENT_SERVICE.register(this);
    }

    private ExecutorService getAsyncExecutor() {
        if (asyncExecutor == null || asyncExecutor.isShutdown()) {
            asyncExecutor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    r -> {
                        Thread t = new Thread(r, "NamiAsyncThread");
                        t.setDaemon(true);
                        return t;
                    }
            );
        }
        return asyncExecutor;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTick(PreTickEvent event) {
        execute(ExecutableThreadType.PRE_TICK);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPostTick(PostTickEvent event) {
        execute(ExecutableThreadType.POST_TICK);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender2D(Render2DEvent event) {
        execute(ExecutableThreadType.RENDER_2D);
    }

    private void execute(ExecutableThreadType type) {
        for (ExecutableRequest req : stateHandler.getActiveRequests()) {

            if (req.ticksDelay > 0)
                req.ticksDelay--;

            if (req.ticksDelay > 0)
                continue;

            try {
                if (req.runnable != null) {
                    if (req.type == ExecutableThreadType.ASYNC) {
                        getAsyncExecutor().submit(() -> {
                            try {
                                req.runnable.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (req.type == type) {
                        req.runnable.run();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (req.repeat) {
                req.ticksDelay = req.initialDelay;
            } else {
                stateHandler.getActiveRequests().remove(req);
            }
        }
    }

    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdownNow();
        }
    }
}
