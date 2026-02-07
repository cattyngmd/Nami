package namidevelopment.kiriyaga.nami.impl.feature.miscellaneous;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.event.impl.AddEntityEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterFeature
public class AnnouncerFeature extends Feature {

    public enum VisualRangeMode {
        NONE, BELL, EXP
    }

    public final BoolSetting joinAnnounce = addSetting(new BoolSetting("JoinAnnounce", false));
    public final BoolSetting joinEveryone = addSetting(new BoolSetting("JoinEveryone", false));
    public final BoolSetting joinFriends = addSetting(new BoolSetting("JoinFriends", true));
    public final BoolSetting visualRange = addSetting(new BoolSetting("VisualRange", false));
    public final BoolSetting rangeEveryone = addSetting(new BoolSetting("RangeEveryone", false));
    public final BoolSetting rangeFriends = addSetting(new BoolSetting("RangeFriends", true));

    public final EnumSetting<VisualRangeMode> soundMode =
            addSetting(new EnumSetting<>("Sound", VisualRangeMode.NONE));

    public AnnouncerFeature() {
        super("Announcer", "Announces in chat when a certain action happened.", FeatureCategory.of("Miscellaneous"), "joinannounce", "joins", "announce", "visualrange");
        soundMode.setShowCondition(visualRange::get);
        joinEveryone.setShowCondition(joinAnnounce::get);
        joinFriends.setShowCondition(joinAnnounce::get);
        rangeEveryone.setShowCondition(visualRange::get);
        rangeFriends.setShowCondition(visualRange::get);
    }

    private boolean validateJoin(String name) {
        boolean b = FRIEND_SERVICE.isFriend(name);
        if (joinEveryone.get() && joinFriends.get()) return true;
        if (joinFriends.get() && b) return true;
        if (joinEveryone.get() && !b) return true;
        return false;
    }

    private boolean validateVisualRange(String name) {
        boolean b = FRIEND_SERVICE.isFriend(name);
        if (rangeEveryone.get() && rangeFriends.get()) return true;
        if (rangeFriends.get() && b) return true;
        if (rangeEveryone.get() && !b) return true;
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!joinAnnounce.get()) return;

        if (event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket joinPacket) {
            if (!joinPacket.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER))
                return;

            for (var entry : joinPacket.entries()) {
                String playerName = entry.profile().name();
                if (playerName == null)
                    continue;

                if (!validateJoin(playerName))
                    continue;
                Component message = CAT_FORMAT.format("{global}" + playerName + " {gray}has joined the game.");
                MC.execute(() -> CHAT_SERVICE.sendPersistent(playerName, message));
            }
        }

        if (event.getPacket() instanceof ClientboundPlayerInfoRemovePacket leavePacket) {
            for (var playerInfo : leavePacket.profileIds()) {
                var info = MC.getConnection().getPlayerInfo(playerInfo);
                if (info == null)
                    continue;

                String playerName = info.getProfile().name();
                if (playerName == null)
                    continue;
                if (!validateJoin(playerName))
                    continue;

                Component message = CAT_FORMAT.format("{global}" + playerName + " {gray}has left the game.");
                MC.execute(() -> CHAT_SERVICE.sendPersistent(playerName, message));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(AddEntityEvent event) {
        if (MC.player == null || MC.level == null || !visualRange.get()) return;

        if (!(MC.level.getEntity(event.getPacket().getId()) instanceof Player player))
            return;

        if (player == MC.player)
            return;

        String name = player.getName().getString();
        if (!validateVisualRange(name))
            return;

        Component message = CAT_FORMAT.format("{global}" + name + " {gray}has entered visual range.");
        MC.execute(() -> CHAT_SERVICE.sendPersistent(player.getStringUUID(), message));
        switch (soundMode.get()) {
            case BELL -> MC.player.playSound(SoundEvents.BELL_BLOCK, 1.0f, 1.0f);
            case EXP -> MC.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            default -> {}
        }
    }
}
