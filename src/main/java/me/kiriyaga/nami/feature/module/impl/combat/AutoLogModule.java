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
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class AutoLogModule extends Module {

    private final IntSetting health = addSetting(new IntSetting("on health", 12, 0, 36));
    private final BoolSetting onRender = addSetting(new BoolSetting("on render", false));
    private final BoolSetting packet = addSetting(new BoolSetting("packet", false));
    private final BoolSetting onPop = addSetting(new BoolSetting("on pop", false));
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
        if (packet.get() && event.getPacket() instanceof EntitySpawnS2CPacket spawnPacket) {
            int id = spawnPacket.getEntityId();
            double x = spawnPacket.getX();
            double y = spawnPacket.getY();
            double z = spawnPacket.getZ();

            MC.execute(() -> {
                Entity entity = MC.world.getEntityById(id);
                if (entity instanceof PlayerEntity player && !FRIEND_MANAGER.isFriend(player.getName().getString())) {
                    double distance = MC.player.distanceTo(player);
                    logOut("Untrusted player in range: " + player.getName().toString() + " (" + String.format("%.1f", distance) + " blocks)");
                }
            });
        }

        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getEntity(MC.world) == MC.player && packet.getStatus() == 35 && onPop.get()) {
                MC.execute(() -> logOut("AutoLog: totem got popped."));
            }
        }
    }


    private void logOut(String reason) {
        if (MODULE_MANAGER.getStorage().getByClass(IllegalDisconnectModule.class).isEnabled()){
            triggerToggle();
            EVENT_MANAGER.post(new DissconectEvent());
        } else {
            if (MC.getNetworkHandler() != null) {
                triggerToggle();
                MC.getNetworkHandler().onDisconnect(new net.minecraft.network.packet.s2c.common.DisconnectS2CPacket(
                        net.minecraft.text.Text.of("AutoLog: §7" + reason)
                ));
            }
        }
    }

    private void triggerToggle(){
        if (selfToggle.get())
            this.toggle();

        if (reconnectToggle.get() && MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class).isEnabled())
            MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class).toggle();
    }
}
