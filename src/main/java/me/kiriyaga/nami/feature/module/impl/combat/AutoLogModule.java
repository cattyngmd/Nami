package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.DissconectEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.misc.AutoReconnectModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.misc.IllegalDisconnectModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.util.EntityUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule(category = "combat")
public class AutoLogModule extends Module {

    private final IntSetting health = addSetting(new IntSetting("on health", 12, 0, 36));
    private final BoolSetting onRender = addSetting(new BoolSetting("on render", false));
    private final BoolSetting packet = addSetting(new BoolSetting("packet", false));
    private final IntSetting onLevel = addSetting(new IntSetting("on level", 0, 0, 15000));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("self toggle", true));
    private final BoolSetting reconnectToggle = addSetting(new BoolSetting("reconnect toggle", true));

    private boolean triggeredLevel = false;

    public AutoLogModule() {
        super("auto log", "Automatically logs out in certain conditions.", ModuleCategory.of("combat"), "autolog", "panic", "logout", "фгещдщп");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(PreTickEvent event) {
        if (MC.player == null || MC.world == null)
            return;

        ClientPlayerEntity player = MC.player;

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

        if (player.getHealth() <= health.get() && health.get() != 0) {
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

            MC.execute(() -> {
                Entity entity = MC.world.getEntityById(id);
                if (entity instanceof PlayerEntity player && !FRIEND_MANAGER.isFriend(player.getName().getString())) {
                    double distance = MC.player.distanceTo(player);
                        logOut("Untrusted player in range: " + player.getName().toString() + " (" + String.format("%.1f", distance) + " blocks)");
                }
            });
        }
    }


    private void logOut(String reason) {
        if (MODULE_MANAGER.getStorage().getByClass(IllegalDisconnectModule.class).isEnabled())
            EVENT_MANAGER.post(new DissconectEvent());
        else {
            if (MC.getNetworkHandler() != null) {
                MC.getNetworkHandler().onDisconnect(new net.minecraft.network.packet.s2c.common.DisconnectS2CPacket(
                        net.minecraft.text.Text.of("AutoLog: §7" + reason)
                ));

                if (selfToggle.get())
                    this.toggle();

                if (reconnectToggle.get() && MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class).isEnabled())
                    MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class).toggle();
            }
        }
    }
}
