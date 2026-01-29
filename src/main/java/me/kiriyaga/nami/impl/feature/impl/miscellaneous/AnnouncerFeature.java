package me.kiriyaga.nami.impl.feature.impl.miscellaneous;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.Feature;

import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.impl.setting.impl.EnumSetting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

import static me.kiriyaga.nami.Nami.*;

@RegisterFeature
public class AnnouncerFeature extends Feature {
    public enum VisualRangeMode {
        NONE, BELL, EXP
    }

    public final BoolSetting everyone = addSetting(new BoolSetting("Everyone", false));
    public final BoolSetting friends = addSetting(new BoolSetting("Friends", true));
    public final BoolSetting joinAnnounce = addSetting(new BoolSetting("JoinAnnounce", false));
    public final BoolSetting visualRange = addSetting(new BoolSetting("VisualRange", false));
    private final EnumSetting<VisualRangeMode> soundMode = addSetting(new EnumSetting<>("Sound", VisualRangeMode.NONE));

    public AnnouncerFeature() {
        super("Announcer", "Announces in chat when a certain action happened.", FeatureCategory.of("Miscellaneous"), "joinannounce", "joins", "announce", "visualrange");
    soundMode.setShowCondition(visualRange::get);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!joinAnnounce.get()) return;

        if (event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket joinPacket) {
            if (joinPacket.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                for (var entry : joinPacket.entries()) {
                    String playerName = entry.profile().name();
                    if (playerName == null) continue;

                    boolean isFriend = FRIEND_SERVICE.isFriend(playerName);

                    if ((everyone.get() && !isFriend) || (friends.get() && isFriend)) {
                        Component message = CAT_FORMAT.format("{g}" + playerName + " {reset}joined the game.");
                        CHAT_SERVICE.sendPersistent(playerName, message);
                    }
                }
            }
        } else if (event.getPacket() instanceof ClientboundPlayerInfoRemovePacket leavePacket) {
            for (var playerInfo : leavePacket.profileIds()) {
                var info = MC.getConnection().getPlayerInfo(playerInfo);
                if (info == null) continue;

                String playerName = info.getProfile().name();
                if (playerName == null) continue;

                boolean isFriend = FRIEND_SERVICE.isFriend(playerName);

                if ((everyone.get() && !isFriend) || (friends.get() && isFriend)) {
                    Component message = CAT_FORMAT.format("{g}" + playerName + " {reset}has left the game.");
                    CHAT_SERVICE.sendPersistent(playerName, message);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (MC.player == null || MC.level == null || !visualRange.get()) return;

        if (event.getEntity() instanceof Player player) {

            if (player == MC.player)
                return;

            boolean isFriend = FRIEND_SERVICE.isFriend(player.getName().getString());

            if (friends.get() && everyone.get()) {
            } else if (friends.get() && isFriend) {
            } else if (everyone.get() && !isFriend) {
            } else {
                return;
            }

            Component message = CAT_FORMAT.format("{g}" + player.getName().getString() + " {reset}has entered visual range.");

            CHAT_SERVICE.sendPersistent(player.getStringUUID(), message);

            switch (soundMode.get()) {
                case BELL -> MC.player.playSound(SoundEvents.BELL_BLOCK, 1.0f, 1.0f);
                case EXP -> MC.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                default -> {}
            }
        }
    }
}
