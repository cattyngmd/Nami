package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketReceiveEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class JoinAnnounceModule extends Module {

    public JoinAnnounceModule() {
        super("join announce", "Announces in chat when a player joins the server.", Category.WORLD, "joinannounce", "joins", "announce", "ощштфттщгтсу");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!isEnabled()) return;

        if (event.getPacket() instanceof PlayerListS2CPacket joinPacket) {
            if (joinPacket.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (var entry : joinPacket.getEntries()) {
                    String playerName = entry.profile().getName();
                    if (playerName == null) continue;

                    String message = "§f[§a+§f] §7" + playerName;

                    CHAT_MANAGER.sendPersistent(playerName, message);
                }
            }
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket leavePacket) {
            for (var playerInfo : leavePacket.profileIds()) {
                var info = MINECRAFT.getNetworkHandler().getPlayerListEntry(playerInfo);

                var playerName = info.getProfile().getName();

                if (playerName == null) continue;

                String message = "§f[§c-§f] §7" + playerName;

                CHAT_MANAGER.sendPersistent(playerName, message);
            }
        }
    }
}
