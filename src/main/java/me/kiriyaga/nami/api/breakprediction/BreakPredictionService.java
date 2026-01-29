package me.kiriyaga.nami.api.breakprediction;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.impl.feature.impl.visuals.BreakHighlightFeature;
import me.kiriyaga.nami.impl.feature.impl.world.SpeedMineFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.nami.Nami.*;

public class BreakPredictionService {

    private final Map<UUID, PlayerBreakState> players = new ConcurrentHashMap<>();

    public void init() { // TODO: this shit doesnt work if player mines with automine
        EVENT_SERVICE.register(this);
        LOGGER.info("Break SERVICE loaded.");
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
            float speed = FEATURE_SERVICE.getStorage().getByClass(SpeedMineFeature.class).speed.get().floatValue();
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

    @SubscribeEvent
    public void onRender3DEvent(Render3DEvent event) {
        if (FEATURE_SERVICE.getStorage().getByClass(BreakHighlightFeature.class).isEnabled()) {
            for (PlayerBreakState state : players.values()) {
                state.render(event);
            }
        }
    }
}
