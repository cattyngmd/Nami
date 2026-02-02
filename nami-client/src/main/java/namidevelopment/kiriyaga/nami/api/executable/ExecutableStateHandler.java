package namidevelopment.kiriyaga.nami.api.executable;

import namidevelopment.kiriyaga.nami.api.executable.model.ExecutableRequest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExecutableStateHandler {
    private final List<ExecutableRequest> activeRequests = new CopyOnWriteArrayList<>();

    public List<ExecutableRequest> getActiveRequests() {
        return activeRequests;
    }

    public void addRequest(ExecutableRequest request) {
        activeRequests.add(request);
    }

    public void removeRequest(ExecutableRequest request) {
        activeRequests.remove(request);
    }

    public void clearAll() {
        activeRequests.clear();
    }
}
