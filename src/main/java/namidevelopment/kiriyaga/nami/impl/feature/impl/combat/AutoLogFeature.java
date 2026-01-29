package namidevelopment.kiriyaga.nami.impl.feature.impl.combat;

import namidevelopment.kiriyaga.nami.api.executable.model.ExecutableThreadType;
import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.DissconectEvent;
import namidevelopment.kiriyaga.nami.event.impl.EntitySpawnEvent;
import namidevelopment.kiriyaga.nami.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.miscellaneous.AutoReconnectFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.exploits.IllegalDisconnectFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import namidevelopment.kiriyaga.nami.util.entity.EntityUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AutoLogFeature extends Feature {

    private final IntSetting health = addSetting(new IntSetting("OnHealth", 12, 0, 36));
    private final BoolSetting onRender = addSetting(new BoolSetting("OnRender", false));
    private final BoolSetting packet = addSetting(new BoolSetting("Packet", false));
    private final BoolSetting onPop = addSetting(new BoolSetting("OnPop", false));
    private final IntSetting onLevel = addSetting(new IntSetting("OnLevel", 0, 0, 15000));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));
    private final BoolSetting reconnectToggle = addSetting(new BoolSetting("ReconnectToggle", true));

    private boolean triggeredLevel = false;
    private boolean loggingOut = false;

    public AutoLogFeature() {
        super("AutoLog", "Automatically logs out in certain conditions.", FeatureCategory.of("Combat"), "autolog", "panic", "logout");
        packet.setShowCondition(() -> onRender.get());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(PreTickEvent event) {
        if (MC.player == null || MC.level == null)
            return;
        this.clearDisplayInfo();

        this.addDisplayInfo(health.get().toString());

        LocalPlayer player = MC.player;

        if (onLevel.get() != 0) {

            if (triggeredLevel && player.getBlockY() <= onLevel.get()) {
                logOut("Too low level: §7" + player.getBlockY() + "§f Blocks");
                triggeredLevel = false;
                return;
            }

            if (player.getBlockY() > onLevel.get()) {
                triggeredLevel = true;
            }
        }

        if (player.getHealth() <= health.get() && health.get() != 0) {
            logOut("Low health: §7" + player.getHealth() + "§f HP");
            return;
        }

        if (onRender.get()) {
            for (Entity other : EntityUtils.getOtherPlayers()) {
                if (!FRIEND_SERVICE.isFriend(other.getName().getString())) {
                    double distance = player.distanceTo(other);
                    logOut("Untrusted player in range: " + player.getName().toString() + " (" + String.format("%.1f", distance) + " blocks)");
                    return;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPacket() instanceof ClientboundEntityEventPacket packet))
            return;

        EXECUTABLE_SERVICE.getRequestHandler().submit(() -> {
            if (MC.player == null || MC.level == null)
                return;

            if (packet.getEntity(MC.level) == MC.player
                    && packet.getEventId() == 35
                    && onPop.get()) {

                logOut("AutoLog: totem got popped.");
            }
        }, 0, ExecutableThreadType.PRE_TICK);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (MC.player == null || MC.level == null || !packet.get() || !onRender.get()) return;

        if (event.getEntity() instanceof Player player) {

            if (player == MC.player)
                return;

            if (FRIEND_SERVICE.isFriend(player.getName().getString()))
                return;

            double distance = MC.player.distanceTo(player);
            logOut("Untrusted player in range: " + player.getName().toString() + " (" + String.format("%.1f", distance) + " blocks)");
        }
    }


    private void logOut(String reason) {
        if (loggingOut)
            return;

        loggingOut = true;
        if (FEATURE_SERVICE.getStorage().getByClass(IllegalDisconnectFeature.class).isEnabled()) {
            triggerToggle();
            EVENT_SERVICE.post(new DissconectEvent());} else {
            if (MC.getConnection() != null) {
                triggerToggle();
                MC.getConnection().handleDisconnect(new ClientboundDisconnectPacket(Component.nullToEmpty("AutoLog: §7" + reason)));
            }
        }
    }


    private void triggerToggle(){
        if (selfToggle.get())
            this.toggle();

        if (reconnectToggle.get() && FEATURE_SERVICE.getStorage().getByClass(AutoReconnectFeature.class).isEnabled())
            FEATURE_SERVICE.getStorage().getByClass(AutoReconnectFeature.class).toggle();
    }
}
