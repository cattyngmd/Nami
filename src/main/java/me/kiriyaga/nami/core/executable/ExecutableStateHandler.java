package me.kiriyaga.nami.core.executable;

import me.kiriyaga.nami.core.executable.model.ExecutableRequest;

import java.util.ArrayList;
import java.util.List;

public class ExecutableStateHandler {
    private final List<ExecutableRequest> activeRequests = new ArrayList<>();

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
