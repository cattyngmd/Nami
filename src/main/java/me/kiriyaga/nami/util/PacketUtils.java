package me.kiriyaga.nami.util;

import me.kiriyaga.nami.mixin.DuckClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;

import static me.kiriyaga.nami.Nami.MC;

public class PacketUtils {

    public static void sendSequencedPacket(PredictiveAction packetCreator) {
        if (MC.level == null || MC.getConnection() == null) {
            return;
        }

        BlockStatePredictionHandler p = ((DuckClientLevel) MC.level).getBlockStatePredictionHandler().startPredicting();

        try (p) {
            int sequence = p.currentSequence();
            MC.getConnection().send(packetCreator.predict(sequence));
        }
    }
}
