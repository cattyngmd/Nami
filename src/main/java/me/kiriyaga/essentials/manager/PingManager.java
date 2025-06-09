package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;

public class PingManager {
    private final Map<Long, Long> pingMap = new ConcurrentHashMap<>();
    private volatile int lastPing = -1;
    private final int MAX_HISTORY = 10;
    private final int[] pingHistory = new int[MAX_HISTORY];
    private int index = 0;
    private int count = 0;
    private volatile long lastUpdated = -1;

    public void init(){
        EVENT_MANAGER.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(Packet<?> packet) {
        if (packet instanceof KeepAliveS2CPacket keepAlive) {
            long id = keepAlive.getId();
            pingMap.put(id, System.currentTimeMillis());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketSend(Packet<?> packet) {
        if (packet instanceof KeepAliveC2SPacket keepAlive) {
            long id = keepAlive.getId();
            Long sentTime = pingMap.remove(id);
            if (sentTime != null) {
                int ping = (int) (System.currentTimeMillis() - sentTime);
                ping = Math.max(0, Math.min(10000, ping));

                pingHistory[index++ % MAX_HISTORY] = ping;
                count = Math.min(count + 1, MAX_HISTORY);

                lastPing = averagePing();
                lastUpdated = System.currentTimeMillis();
            }
        }
    }

    private int averagePing() {
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += pingHistory[i];
        }
        return count == 0 ? -1 : sum / count;
    }
}
