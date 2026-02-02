package namidevelopment.kiriyaga.api.util;

import namidevelopment.kiriyaga.api.mixin.DuckClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class PacketUtils {

    public static void sendSequencedPacket(PredictiveAction packetCreator) {
        if (API_MC.level == null || API_MC.getConnection() == null) {
            return;
        }

        BlockStatePredictionHandler p = ((DuckClientLevel) API_MC.level).getBlockStatePredictionHandler().startPredicting();

        try (p) {
            int sequence = p.currentSequence();
            API_MC.getConnection().send(packetCreator.predict(sequence));
        }
    }
}
