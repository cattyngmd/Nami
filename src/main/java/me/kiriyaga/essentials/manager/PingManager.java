package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.feature.module.impl.client.PingManagerModule;
import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.EVENT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class PingManager {
    private final Map<Long, Long> pingMap = new ConcurrentHashMap<>();
    private volatile int lastPing = -1;

    private int[] pingHistory = new int[10];
    private int index = 0;
    private int count = 0;
    private volatile long lastUpdated = -1;

    public void init(){
        EVENT_MANAGER.register(this);
        CHAT_MANAGER.sendRaw("[PingManager] Registered event listeners");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(Packet<?> packet) {
        if (packet instanceof KeepAliveS2CPacket keepAlive) {
            long id = keepAlive.getId();
            pingMap.put(id, System.currentTimeMillis());
            CHAT_MANAGER.sendRaw("[PingManager] Received KeepAliveS2CPacket id=" + id);
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

                CHAT_MANAGER.sendRaw("[PingManager] KeepAliveC2SPacket sent id=" + id + ", ping=" + ping + "ms");

                PingManagerModule config = MODULE_MANAGER.getModule(PingManagerModule.class);
                int smoothingStrength = config != null ? config.smoothingStrength.get() : 10;

                if (pingHistory.length != smoothingStrength) {
                    CHAT_MANAGER.sendRaw("[PingManager] Adjusting pingHistory size from " + pingHistory.length + " to " + smoothingStrength);
                    int[] newHistory = new int[smoothingStrength];
                    for (int i = 0; i < Math.min(count, smoothingStrength); i++) {
                        newHistory[i] = pingHistory[(index - count + i + pingHistory.length) % pingHistory.length];
                    }
                    pingHistory = newHistory;
                    count = Math.min(count, smoothingStrength);
                    index = count % smoothingStrength;
                }

                pingHistory[index++ % smoothingStrength] = ping;
                count = Math.min(count + 1, smoothingStrength);

                lastPing = averagePing();
                lastUpdated = System.currentTimeMillis();

                CHAT_MANAGER.sendRaw("[PingManager] Updated average ping: " + lastPing + "ms");
            } else {
                CHAT_MANAGER.sendRaw("[PingManager] KeepAliveC2SPacket sent id=" + id + " but no matching KeepAliveS2CPacket found");
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

    public int getPing() {
        return lastPing;
    }

    public boolean isConnectionUnstable() {
        PingManagerModule config = MODULE_MANAGER.getModule(PingManagerModule.class);
        if (config == null) return false;

        int timeoutMillis = config.unstableConnectionTimeout.get() * 1000;
        if (lastUpdated == -1) {
            CHAT_MANAGER.sendRaw("[PingManager] Connection unstable: no ping data yet");
            return true;
        }
        boolean unstable = (System.currentTimeMillis() - lastUpdated) > timeoutMillis;
        if (unstable) {
            CHAT_MANAGER.sendRaw("[PingManager] Connection unstable: last ping updated " + (System.currentTimeMillis() - lastUpdated) + "ms ago");
        }
        return unstable;
    }
}
