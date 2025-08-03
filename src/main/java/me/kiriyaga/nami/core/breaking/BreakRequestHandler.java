package me.kiriyaga.nami.core.breaking;

import me.kiriyaga.nami.core.breaking.model.BreakTarget;
import net.minecraft.util.math.BlockPos;
import java.util.*;

public class BreakRequestHandler {
    private final List<BreakTarget> targets = new LinkedList<>();
    private final BreakStateHandler stateHandler;

    public BreakRequestHandler(BreakStateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    public void addBlock(BlockPos pos) {
        if (targets.stream().noneMatch(t -> t.getPos().equals(pos))) {
            targets.add(new BreakTarget(pos));
        }
    }

    public void removeBlock(BlockPos pos) {
        targets.removeIf(t -> t.getPos().equals(pos));
    }

    public void clear() {
        targets.clear();
    }

    public void removeExpiredTargets(long maxAgeMillis) {
        long now = System.currentTimeMillis();
        targets.removeIf(t -> now - t.getTimestamp() > maxAgeMillis);
    }


    public List<BreakTarget> getTargets() {
        return targets;
    }

    public boolean hasTarget() {
        return !targets.isEmpty();
    }
}
