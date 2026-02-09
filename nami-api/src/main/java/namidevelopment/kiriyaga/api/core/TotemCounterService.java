package namidevelopment.kiriyaga.api.core;

import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.event.impl.TotemPopEvent;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class TotemCounterService {

    private final Map<Integer, List<Long>> poppedMap = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastPopMap = new ConcurrentHashMap<>();
    private final Map<Integer, Long> popCounters = new ConcurrentHashMap<>();

    private final Set<Integer> lastTickAlive = ConcurrentHashMap.newKeySet();

    public void init() {
        EVENT_SERVICE.register(this);
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.level == null || MC.player == null) return;

        death();
        cleanupNonRenderedPlayers();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (MC.level == null || MC.player == null)
            return;

        if (!(event.getPacket() instanceof ClientboundEntityEventPacket packet))
            return;

        Entity entity = packet.getEntity(MC.level);
        if (!(entity instanceof Player))
            return;

        int id = entity.getId();
        byte eventId = packet.getEventId();

        if (eventId == 35) {
            pop(id);
        }
    }

    private void pop(int entityId) {
        long popId = popCounters.merge(entityId, 1L, Long::sum);

        poppedMap.computeIfAbsent(entityId, k -> new CopyOnWriteArrayList<>()).add(popId);
        lastPopMap.put(entityId, System.currentTimeMillis());

        Entity ent = MC.level.getEntity(entityId);
        if (ent instanceof Player player) {
            int pops = getPoppedTotemCount(entityId);
            EVENT_SERVICE.post(new TotemPopEvent(entityId, player.getName().getString(), player, pops));
        }
    }

    private void death() {
        Set<Integer> currentlyAlive = new HashSet<>();

        for (Entity ent : MC.level.entitiesForRendering()) {
            if (!(ent instanceof Player player)) continue;

            int id = player.getId();
            if (!player.isDeadOrDying()) {
                currentlyAlive.add(id);
                continue;
            }
            if (lastTickAlive.contains(id)) {
                clear(id);
            }
        }
        lastTickAlive.clear();
        lastTickAlive.addAll(currentlyAlive);
    }

    private void cleanupNonRenderedPlayers() {
        if (MC.level == null) return;

        Set<Integer> aliveIds = new HashSet<>();
        for (Entity e : MC.level.entitiesForRendering()) {
            aliveIds.add(e.getId());
        }
        poppedMap.keySet().removeIf(id -> !aliveIds.contains(id));
        lastPopMap.keySet().removeIf(id -> !aliveIds.contains(id));
        popCounters.keySet().removeIf(id -> !aliveIds.contains(id));
        lastTickAlive.removeIf(id -> !aliveIds.contains(id));
    }

    public int getPoppedTotemCount(int entityId) {
        List<Long> list = poppedMap.get(entityId);
        return list == null ? 0 : list.size();
    }

    public List<Long> getPoppedTotemIds(int entityId) {
        List<Long> list = poppedMap.get(entityId);
        if (list == null) return Collections.emptyList();
        return new ArrayList<>(list);
    }

    public boolean hasPoppedTotems(int entityId) {
        return getPoppedTotemCount(entityId) > 0;
    }

    public long getLastPopTime(int entityId) {
        return lastPopMap.getOrDefault(entityId, -1L);
    }

    public void clear(int entityId) {
        poppedMap.remove(entityId);
        lastPopMap.remove(entityId);
        popCounters.remove(entityId);
    }

    public void clearAll() {
        poppedMap.clear();
        lastPopMap.clear();
        popCounters.clear();
        lastTickAlive.clear();
    }
}
