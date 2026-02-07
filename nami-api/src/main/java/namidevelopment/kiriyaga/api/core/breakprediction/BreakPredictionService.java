package namidevelopment.kiriyaga.api.core.breakprediction;

import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static namidevelopment.kiriyaga.api.NamiApi.*;

public class BreakPredictionService {

    private final Map<UUID, PlayerBreakState> players = new ConcurrentHashMap<>();

    public void init() { // TODO: this shit doesnt work if player mines with automine
        EVENT_SERVICE.register(this);
    }

    public PlayerBreakState get(UUID uuid) {
        return players.computeIfAbsent(uuid, PlayerBreakState::new);
    }
    public Collection<PlayerBreakState> all() {
        return players.values();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        MC.execute(() -> {
        if (event.getPacket() instanceof ClientboundBlockDestructionPacket packet) {
//            if (packet.getProgress() != 0)
//                return;

            int entityId = packet.getId();
            BlockPos pos = packet.getPos();

            Player player = (Player) MC.level.getEntity(entityId);
            if (player == null) return;
            UUID uuid = player.getUUID();
            float speed = 0.7f;
            this.get(uuid).startBreak(pos, Direction.UP, speed);
        }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.level == null) return;

        for (PlayerBreakState state : players.values()) {
            state.onTick();
        }
    }

/*    @SubscribeEvent
    public void onRender3DEvent(Render3DEvent event) {
        if (FEATURE_SERVICE.getStorage().getByClass(BreakHighlightFeature.class).isEnabled()) {
            for (PlayerBreakState state : players.values()) {
                state.render(event);
            }
        }
    }*/
}
