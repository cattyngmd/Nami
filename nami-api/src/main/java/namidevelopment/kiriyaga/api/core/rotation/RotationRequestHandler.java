package namidevelopment.kiriyaga.api.core.rotation;

import namidevelopment.kiriyaga.api.core.rotation.model.RotationRequest;
import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.api.util.RotationUtils.yawDifference;

import namidevelopment.kiriyaga.api.client.RotationsFeature;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * Request handler.
 * <p>
 * Stores active {@link RotationRequest}, and prioritize them,
 * Also provides as-is data about complition, current state, based on latest comparing of rotation states.
 */
public class RotationRequestHandler {

    /**
     * rotation request queue sorted by {@link RotationRequest#priority}.
     * FIRST ELEMENT OF LIST IS ALWAYS ACTIVE REQUESET
     */
    private final List<RotationRequest> requests = new ArrayList<>();

    private final RotationStateHandler stateHandler;

    /**
     * Last interpolated, and used, by TickHandler request.
     * We using it to understand, when request was not finished/returned/cancelled, but changed for other request
     */
    private String lastActiveRequestId = null;

    public RotationRequestHandler(RotationStateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    /**
     * Submits new {@link RotationRequest}
     * <p>
     * Prioritize them
     * YOU SHOULD NEVER SUMBIT MORE THEN ONE DYNAMIC REQUEST!
     * USE STATIC REQUEST AND UPDATE DATA ON ANY PRE-TICK EVENT HIGHER THEN LOWEST
     */
    public void submit(RotationRequest request) {
//        RotationFeature.RotationMode mode = Feature_SERVICE.getStorage().getByClass(RotationFeature.class).rotation.get();

        if (request.rotationMode == RotationsFeature.RotationMode.SILENT) {
            performSilent(request);
            stateHandler.setSilentSyncRequired(true);
            return;
        }

        requests.removeIf(r -> Objects.equals(r.id, request.id));
        requests.add(request);
        requests.sort(Comparator.comparingInt(r -> -r.priority));
    }

    public boolean hasRequest(String id) {
        return requests.stream().anyMatch(r -> r.id.equals(id));
    }

    /**
     * Cancels request by ID.
     * YOU SHOULD NEVER CANCEL STATIC REQUESTS
     * it is used only for dynamic request lifecycle
     * @param id identifier of request
     */
    public void cancel(String id) {
        requests.removeIf(r -> r.id.equals(id));
    }

    /**
     * As-Is check for complition.
     *
     * @param id identifier of request
     * @return {@code true}, yaw + pitch is close enough to target (enough = threshold)
     */
    public boolean isCompleted(String id) {
        return isCompleted(
                id,
                FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class)
                        .rotationThreshold.get().floatValue()
        );
    }

    /**
     * Checks if request completed by threshold
     *
     * @param id identifier
     * @param threshold allowed degree loss
     * @return {@code true}, if yaw pitch is close enough to target
     */
    public boolean isCompleted(String id, float threshold) {
        if (FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).rotation.get() == RotationsFeature.RotationMode.SILENT && stateHandler.getSilentSyncRequired())
            return true; // TODO: find better solution
        return requests.stream()
                .filter(r -> r.id.equals(id))
                .findFirst()
                .map(r -> {
                    float yawDiff = yawDifference(r.targetYaw, stateHandler.getRotationYaw());
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

    public void clear() {
        requests.clear();
        lastActiveRequestId = null;
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

    private void performSilent(RotationRequest req) {
        float targetYaw = req.targetYaw;
        float targetPitch = req.targetPitch;

        if (FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).jitter.get()) {
            float minJitter = (float) (FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).rotationThreshold.get() / 4f);
            float maxJitter = (float) (FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class).rotationThreshold.get() / 2);
            float jitterYaw = minJitter + (float) (Math.random() * (maxJitter - minJitter));
            float jitterPitch = minJitter + (float) (Math.random() * (maxJitter - minJitter));
            jitterYaw *= Math.random() < 0.5 ? -1 : 1;
            jitterPitch *= Math.random() < 0.5 ? -1 : 1;

            targetYaw += jitterYaw;
            targetPitch += jitterPitch;

            targetPitch = Mth.clamp(targetPitch, -90f, 90f);
        }

//        ROTATION_SERVICE.getStateHandler().setRotationYaw(targetYaw);
//        ROTATION_SERVICE.getStateHandler().setRotationPitch(targetPitch);
        ROTATION_SERVICE.getStateHandler().setServerYaw(targetYaw);
        ROTATION_SERVICE.getStateHandler().setServerPitch(targetPitch);

        API_MC.getConnection().send(new ServerboundMovePlayerPacket.PosRot(API_MC.player.getX(), API_MC.player.getY(), API_MC.player.getZ(), targetYaw, targetPitch, API_MC.player.onGround(), true));
    }
}
