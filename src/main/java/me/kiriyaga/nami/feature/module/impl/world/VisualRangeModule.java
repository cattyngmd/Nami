package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class VisualRangeModule extends Module {

    private final BoolSetting friends = addSetting(new BoolSetting("friends", false));
    private final BoolSetting sound = addSetting(new BoolSetting("sound", false));

    public VisualRangeModule() {
        super("visual range", "Notifies you when players enter render distance.", ModuleCategory.of("world"), "visualrange", "мшыгфдкфтпу");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(EntitySpawnEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (event.getEntity() instanceof PlayerEntity player) {

            if (player == MC.player)
                return;

            if (FRIEND_MANAGER.isFriend(player.getName().getString()) && !friends.get())
                return;

            CHAT_MANAGER.sendPersistent(player.getUuidAsString(), "§7" + player.getName().getString() + "§f has entered visual range.");

            if (sound.get())
                MC.player.playSound(SoundEvents.BLOCK_BELL_USE, 1.0f, 1.0f);
        }
    }
}
