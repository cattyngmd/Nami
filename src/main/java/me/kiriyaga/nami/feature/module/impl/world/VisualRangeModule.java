package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.module.impl.world.VisualRangeModule.VisualRangeMode.BELL;
import static me.kiriyaga.nami.feature.module.impl.world.VisualRangeModule.VisualRangeMode.EXP;

@RegisterModule
public class VisualRangeModule extends Module {

    public enum VisualRangeMode {
        BELL, EXP
    }

    private final BoolSetting friends = addSetting(new BoolSetting("friends", false));
    private final BoolSetting sound = addSetting(new BoolSetting("sound", false));
    private final EnumSetting soundMode = addSetting(new EnumSetting("sound", EXP));

    public VisualRangeModule() {
        super("visual range", "Notifies you when players enter render distance.", ModuleCategory.of("world"), "visualrange");
        soundMode.setShowCondition(() -> sound.get());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (event.getEntity() instanceof PlayerEntity player) {

            if (player == MC.player)
                return;

            if (FRIEND_MANAGER.isFriend(player.getName().getString()) && !friends.get())
                return;

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
