package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;

import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class JoinAnnounceModule extends Module {

    public final BoolSetting everyone = addSetting(new BoolSetting("everyone", false));
    public final BoolSetting friends = addSetting(new BoolSetting("friends", true));

    public JoinAnnounceModule() {
        super("join announce", "Announces in chat when a certain player joins the server.", ModuleCategory.of("misc"), "joinannounce", "joins", "announce", "ощштфттщгтсу");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) return;

        if (event.getPacket() instanceof PlayerListS2CPacket joinPacket) {
            if (joinPacket.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (var entry : joinPacket.getEntries()) {
                    String playerName = entry.profile().getName();
                    if (playerName == null) continue;

                    boolean isFriend = FRIEND_MANAGER.isFriend(playerName);

                    if ((everyone.get() && !isFriend) || (friends.get() && isFriend)) {
                        String message = "§7" + playerName + " joined the game";
                        CHAT_MANAGER.sendPersistent(playerName, message);
                    }
                }
            }
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket leavePacket) {
            for (var playerInfo : leavePacket.profileIds()) {
                var info = MC.getNetworkHandler().getPlayerListEntry(playerInfo);
                if (info == null) continue;

                String playerName = info.getProfile().getName();
                if (playerName == null) continue;

                boolean isFriend = FRIEND_MANAGER.isFriend(playerName);

                if ((everyone.get() && !isFriend) || (friends.get() && isFriend)) {
                    String message = "§7" + playerName + " left the game";
                    CHAT_MANAGER.sendPersistent(playerName, message);
                }
            }
        }
    }
}
