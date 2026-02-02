package namidevelopment.kiriyaga.api.core;

import namidevelopment.kiriyaga.api.client.LatencyFeature;
import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.event.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.debugchart.LocalSampleLogger;

import java.util.Arrays;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class ServerService {

    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private int countTick = 0;
    private long lastTimeUpdate = -1;

    private Vec3 lastSetbackPosition;
    private long lastSetbackTime;
    private int lastTeleportId;
    private final int[] pendingTransactions = new int[4];
    private int transactionIndex;
    private int ping = -1;

    private volatile long lastReceiveTime = -1;
    private volatile int lastPing = -1;
    private int[] pingHistory = new int[10];
    private int index = 0;
    private int countPing = 0;
    private volatile long lastUpdated = -1;

    public void init() {
        EVENT_SERVICE.register(this);
        Arrays.fill(pendingTransactions, -1);
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (API_MC.level == null ||  API_MC.getConnection() == null)
            return;

        updatePing();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            long now = System.currentTimeMillis();

            if (lastTimeUpdate != -1) {
                float elapsed = (now - lastTimeUpdate) / 1000.0f;
                float tps = 20.0f / elapsed;
                tickRates[nextIndex % tickRates.length] = Math.min(Math.max(tps, 0.0f), 20.0f);
                nextIndex++;
                countTick = Math.min(countTick + 1, tickRates.length);
            }

            lastTimeUpdate = now;
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPacketReceive2(PacketReceiveEvent event) {
        if (event.getPacket() instanceof ClientboundPingPacket packet) {
            if (transactionIndex > 3) return;

            pendingTransactions[transactionIndex] = packet.getId();
            transactionIndex++;
        } else if (event.getPacket() instanceof ClientboundPlayerPositionPacket packet) {
            lastSetbackPosition = packet.change().position();
            lastSetbackTime = System.currentTimeMillis();
            lastTeleportId = packet.id();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive3(PacketReceiveEvent packet) {
        LatencyFeature config = FEATURE_SERVICE.getStorage().getByClass(LatencyFeature.class);

        if (config.fastLatencyMode.get() != LatencyFeature.mode.OLD)
            return;

        if (packet.getPacket() instanceof ClientboundKeepAlivePacket) {
            long now = System.currentTimeMillis();
            int keepAliveInterval = config != null ? config.keepAliveInterval.get() : 1000;

            if (lastReceiveTime != -1) {
                long interval = now - lastReceiveTime;
                int ping = (int) Math.max(0, interval - keepAliveInterval);

                int smoothingStrength = config != null ? config.smoothingStrength.get() : 10;
                if (pingHistory.length != smoothingStrength) {
                    int[] newHistory = new int[smoothingStrength];
                    for (int i = 0; i < Math.min(countPing, smoothingStrength); i++) {
                        newHistory[i] = pingHistory[(index - countPing + i + pingHistory.length) % pingHistory.length];
                    }
                    pingHistory = newHistory;
                    countPing = Math.min(countPing, smoothingStrength);
                    index = countPing % smoothingStrength;
                }

                pingHistory[index++ % smoothingStrength] = ping;
                countPing = Math.min(countPing + 1, smoothingStrength);

                updatePing(averagePing());
            }

            lastReceiveTime = now;
        }
    }

    public float getAverageTPS() {
        if (countTick == 0) return 20.0f;

        float sum = 0.0f;
        int valid = 0;
        for (int i = 0; i < countTick; i++) {
            float t = tickRates[i];
            if (t > 0.0f) {
                sum += t;
                valid++;
            }
        }
        return valid == 0 ? 20.0f : Math.min(Math.max(sum / valid, 0.0f), 20.0f);
    }

    public float getMinTPS() {
        if (countTick == 0) return 20.0f;

        float min = 20.0f;
        for (int i = 0; i < countTick; i++) {
            float t = tickRates[i];
            if (t > 0.0f && t < min) {
                min = t;
            }
        }
        return Math.min(Math.max(min, 0.0f), 20.0f);
    }

    public float getLatestTPS() {
        if (countTick == 0) return 20.0f;

        int last = (nextIndex - 1 + tickRates.length) % tickRates.length;
        return Math.min(Math.max(tickRates[last], 0.0f), 20.0f);
    }

    public boolean hasElapsedSinceSetback(long milliseconds) {
        return lastSetbackPosition != null && (System.currentTimeMillis() - lastSetbackTime) >= milliseconds;
    }

    public Vec3 getLastSetbackPosition() {
        return lastSetbackPosition;
    }

    public long getLastSetbackTime() {
        return lastSetbackTime;
    }

    public int getLastTeleportId() {
        return lastTeleportId;
    }

    private int averagePing() {
        int sum = 0;
        for (int i = 0; i < countPing; i++) {
            sum += pingHistory[i];
        }
        return countPing == 0 ? -1 : sum / countPing;
    }

    public int getPing() {
        return ping;
    }

    public void updatePing() {
        LatencyFeature config = FEATURE_SERVICE.getStorage().getByClass(LatencyFeature.class);
        if (config == null) ping = lastPing;

        switch (config.fastLatencyMode.get()) {
            case OLD:
                ping = lastPing;
                break;
            case OFF:
                if (API_MC.getConnection() != null && API_MC.player != null) {
                    ping = API_MC.getConnection().getPlayerInfo(API_MC.player.getUUID()).getLatency();
                } else {
                    ping = -1;
                }
                break;
            case NEW:
                try {
                    if (API_MC.getDebugOverlay() != null && API_MC.getDebugOverlay().getPingLogger() != null) {
                        LocalSampleLogger pingLog = API_MC.getDebugOverlay().getPingLogger();
                        int count = pingLog.size();
                        if (count == 0) ping = -1;

                        updatePing((int) pingLog.get(count - 1, 0));

                        ping = (int) pingLog.get(count - 1, 0);
                    }
                } catch (Exception ignored) {
                }
                break;
            default: ping = -1;
        }
    }

    public boolean isConnectionUnstable() {
        LatencyFeature config = FEATURE_SERVICE.getStorage().getByClass(LatencyFeature.class);
        if (config == null) return false;

        int timeoutMillis = config.unstableConnectionTimeout.get() * 1000;

        if (lastUpdated == -1) {
            return true;
        }

        boolean unstable = (System.currentTimeMillis() - lastUpdated) > timeoutMillis;
        return unstable;
    }

    public void updatePing(int ping) {
        if (ping != lastPing) {
            lastPing = ping;
            lastUpdated = System.currentTimeMillis();
        }
    }

    public float getUnstableTime() {
        if (lastUpdated == -1) return Float.POSITIVE_INFINITY;
        long deltaMillis = System.currentTimeMillis() - lastUpdated;
        return deltaMillis / 1000.0f;
    }
}
