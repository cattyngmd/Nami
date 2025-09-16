package me.kiriyaga.nami.core.executable;

import me.kiriyaga.nami.core.executable.model.ExecutableEventType;
import me.kiriyaga.nami.core.executable.model.ExecutableRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;

import java.util.Iterator;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;

public class ExecutableTickHandler {

    private final ExecutableStateHandler stateHandler;
    private final ExecutableRequestHandler requestHandler;

    public ExecutableTickHandler(ExecutableStateHandler stateHandler, ExecutableRequestHandler requestHandler) {
        this.stateHandler = stateHandler;
        this.requestHandler = requestHandler;
    }

    public void init() {
        EVENT_MANAGER.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOW) // low for now, until event arhc rewrite
    public void onPreTick(PreTickEvent event) {
        execute(ExecutableEventType.PRE_TICK);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        execute(ExecutableEventType.POST_TICK);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRender2D(Render2DEvent event) {
        execute(ExecutableEventType.RENDER_2D);
    }

    private void execute(ExecutableEventType type) {
        Iterator<ExecutableRequest> it = stateHandler.getActiveRequests().iterator();
        while (it.hasNext()) {
            ExecutableRequest req = it.next();
            if (req.type != type) continue;

            if (req.ticksDelay > 0) {
                req.ticksDelay--;
                continue;
            }

            try {
                if (req.runnable != null) req.runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (req.repeat) {
                req.ticksDelay = req.initialDelay;
            } else {
                it.remove();
            }
        }
    }
}
