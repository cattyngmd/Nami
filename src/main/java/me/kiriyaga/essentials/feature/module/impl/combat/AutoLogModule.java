package me.kiriyaga.essentials.feature.module.impl.combat;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.world.AutoReconnectModule;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import me.kiriyaga.essentials.util.EntityUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

import static me.kiriyaga.essentials.Essentials.*;

public class AutoLogModule extends Module {

    private final IntSetting health = addSetting(new IntSetting("on health", 12, 1, 26));
    private final BoolSetting onRender = addSetting(new BoolSetting("on render", false));
    private final BoolSetting packet = addSetting(new BoolSetting("packet", false));
    private final IntSetting onLevel = addSetting(new IntSetting("on level", 0, 0, 15000));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("self toggle", true));
    private final BoolSetting reconnectToggle = addSetting(new BoolSetting("reconnect toggle", true));

    private boolean triggeredLevel = false;

    public AutoLogModule() {
        super("auto log", "Logs out when danger is near.", Category.COMBAT, "autolog", "panic", "logout", "фгещдщп");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(PreTickEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null)
            return;

        ClientPlayerEntity player = MINECRAFT.player;

        if (onLevel.get() != 0) {

            if (triggeredLevel && player.getBlockZ() <= onLevel.get()) {
                logOut("Too low level: §7" + player.getBlockZ() + "§f Blocks");
                triggeredLevel = false;
                return;
            }

            if (player.getBlockZ() > onLevel.get()) {
                triggeredLevel = true;
            }
        }
        
        if (player.getHealth() <= health.get()) {
            logOut("Low health: §7" + player.getHealth() + "§f HP");
            return;
        }

        if (onRender.get()) {
            for (Entity other : EntityUtils.getOtherPlayers()) {
                if (!FRIEND_MANAGER.isFriend(other.getName().getString())) {
                    double distance = player.distanceTo(other);
                    logOut("Untrusted player in range: " + player.getName().toString() + " (" + String.format("%.1f", distance) + " blocks)");
                    return;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (packet.get() && event.getPacket() instanceof EntitySpawnS2CPacket packet) {
            int id = packet.getEntityId();
            double x = packet.getX();
            double y = packet.getY();
            double z = packet.getZ();

            MINECRAFT.execute(() -> {
                Entity entity = MINECRAFT.world.getEntityById(id);
                if (entity instanceof PlayerEntity player && !FRIEND_MANAGER.isFriend(player.getName().getString())) {
                    double distance = MINECRAFT.player.distanceTo(player);
                        logOut("Untrusted player in range: " + player.getName().toString() + " (" + String.format("%.1f", distance) + " blocks)");
                }
            });
        }
    }


    private void logOut(String reason) {
        if (MINECRAFT.getNetworkHandler() != null) {
            MINECRAFT.getNetworkHandler().onDisconnect(new net.minecraft.network.packet.s2c.common.DisconnectS2CPacket(
                    net.minecraft.text.Text.of("AutoLog: §7" + reason)
            ));

            if (selfToggle.get())
                this.toggle();

            if (reconnectToggle.get() && MODULE_MANAGER.getModule(AutoReconnectModule.class).isEnabled())
                MODULE_MANAGER.getModule(AutoReconnectModule.class).toggle();
        }
    }
}
