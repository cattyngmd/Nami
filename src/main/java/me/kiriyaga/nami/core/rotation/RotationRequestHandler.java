package me.kiriyaga.nami.core.rotation;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.feature.module.impl.client.RotationManagerModule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class RotationRequestHandler {

    private final List<RotationRequest> requests = new ArrayList<>();
    private final RotationStateHandler stateHandler;
    private String lastActiveRequestId = null;

    public RotationRequestHandler(RotationStateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    public void submit(RotationRequest request) {
        requests.removeIf(r -> Objects.equals(r.id, request.id));
        requests.add(request);
        requests.sort(Comparator.comparingInt(r -> -r.priority));
    }

    public boolean hasRequest(String id) {
        return requests.stream().anyMatch(r -> r.id.equals(id));
    }

    public void cancel(String id) {
        requests.removeIf(r -> r.id.equals(id));
    }

    public boolean isCompleted(String id) {
        return isCompleted(id, MODULE_MANAGER.getStorage().getByClass(RotationManagerModule.class).rotationThreshold.get().floatValue());
    }

    public boolean isCompleted(String id, float threshold) {
        return requests.stream()
                .filter(r -> r.id.equals(id))
                .findFirst()
                .map(r -> {
                    float yawDiff = wrapDegrees(r.targetYaw - stateHandler.getRotationYaw());
                    float pitchDiff = r.targetPitch - stateHandler.getRotationPitch();
                    return Math.abs(yawDiff) <= threshold && Math.abs(pitchDiff) <= threshold;
                }).orElse(false);
    }

    public RotationRequest getActiveRequest() {
        return requests.isEmpty() ? null : requests.get(0);
    }

    public void removeActiveRequest() {
        if (!requests.isEmpty()) {
            requests.remove(0);
        }
    }

    private float wrapDegrees(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    public void clearLastActiveId() {
        lastActiveRequestId = null;
    }

    public String getLastActiveId() {
        return lastActiveRequestId;
    }

    public void setLastActiveId(String id) {
        lastActiveRequestId = id;
    }
}
