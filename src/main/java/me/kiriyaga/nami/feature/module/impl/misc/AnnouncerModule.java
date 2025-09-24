package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;

import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.module.impl.world.VisualRangeModule.VisualRangeMode.BELL;
import static me.kiriyaga.nami.feature.module.impl.world.VisualRangeModule.VisualRangeMode.EXP;

@RegisterModule
public class AnnouncerModule extends Module {
    public enum VisualRangeMode {
        BELL, EXP
    }

    public final BoolSetting everyone = addSetting(new BoolSetting("Everyone", false));
    public final BoolSetting friends = addSetting(new BoolSetting("Friends", true));
    public final BoolSetting joinAnnounce = addSetting(new BoolSetting("JoinAnnounce", false));
    public final BoolSetting visualRange = addSetting(new BoolSetting("VisualRange", false));
    private final BoolSetting sound = addSetting(new BoolSetting("Sound", false));
    private final EnumSetting soundMode = addSetting(new EnumSetting("Sound", EXP));

    public AnnouncerModule() {
        super("Announcer", "Announces in chat when a certain action happened.", ModuleCategory.of("Misc"), "joinannounce", "joins", "announce");
    sound.setShowCondition(visualRange::get);
    soundMode.setShowCondition(visualRange::get);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!joinAnnounce.get()) return;

        if (event.getPacket() instanceof PlayerListS2CPacket joinPacket) {
            if (joinPacket.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                for (var entry : joinPacket.getEntries()) {
                    String playerName = entry.profile().getName();
                    if (playerName == null) continue;

                    boolean isFriend = FRIEND_MANAGER.isFriend(playerName);

                    if ((everyone.get() && !isFriend) || (friends.get() && isFriend)) {
                        Text message = CAT_FORMAT.format("{g}" + playerName + " {reset}joined the game.");
                        CHAT_MANAGER.sendPersistent(playerName, message);
                    }
                }
            }
        } else if (event.getPacket() instanceof PlayerRemoveS2CPacket leavePacket) {
            for (var playerInfo : leavePacket.comp_1105()) {
                var info = MC.getNetworkHandler().getPlayerListEntry(playerInfo);
                if (info == null) continue;

                String playerName = info.getProfile().getName();
                if (playerName == null) continue;

                boolean isFriend = FRIEND_MANAGER.isFriend(playerName);

                if ((everyone.get() && !isFriend) || (friends.get() && isFriend)) {
                    Text message = CAT_FORMAT.format("{g}" + playerName + " {reset}has left the game.");
                    CHAT_MANAGER.sendPersistent(playerName, message);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (MC.player == null || MC.world == null || visualRange.get()) return;

        if (event.getEntity() instanceof PlayerEntity player) {

            if (player == MC.player)
                return;

            boolean isFriend = FRIEND_MANAGER.isFriend(player.getName().getString());

            if (friends.get() && everyone.get()) {
            } else if (friends.get() && isFriend) {
            } else if (everyone.get() && !isFriend) {
            } else {
                return;
            }

            CHAT_MANAGER.sendPersistent(player.getUuidAsString(),
                    CAT_FORMAT.format("{g}" + player.getName().getString() + " {reset}has entered visual range."));

            if (sound.get()) {
                switch (soundMode.get()) {
                    case BELL -> MC.player.playSound(SoundEvents.BLOCK_BELL_USE, 1.0f, 1.0f);
                    case EXP -> MC.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    default -> { }
                }
            }
        }
    }
}
