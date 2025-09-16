package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.OpenScreenEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoRespawnModule extends Module {

    public final BoolSetting sendCords = addSetting(new BoolSetting("send cords", true));

    public AutoRespawnModule() {
        super("auto respawn", "Automatically respawns after death.", ModuleCategory.of("misc"), "autorespawn");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onOpenScreen(OpenScreenEvent event) {
        if (!(event.getScreen() instanceof DeathScreen)) return;
        if (MC == null || MC.player == null) return;

        if (sendCords.get()) {
            Vec3d pos = MC.player.getPos();
            String coords = String.format("X: %d Y: %d Z: %d",
                    Math.round(pos.x), Math.round(pos.y), Math.round(pos.z));

            CHAT_MANAGER.sendPersistent(AutoRespawnModule.class.getName(), "Death coordinates: {g}" + coords+"{reset}.");
        }

        MC.player.requestRespawn();
        event.cancel();
    }
}
