package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.OpenScreenEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class RespawnModule extends Module {

    public final BoolSetting sendCords = addSetting(new BoolSetting("LogCords", true));
    public final BoolSetting autoRespawn = addSetting(new BoolSetting("AutoRespawn", false));

    public RespawnModule() {
        super("Respawn", "Death screen tweaks.", ModuleCategory.of("Miscellaneous"), "autorespawn");
    }

    boolean b = false;

    @Override
    public void onEnable() {
        b = false;
    }
    @SubscribeEvent
    public void onTick(PreTickEvent ev) {
        if (MC.player == null || !MC.player.isDeadOrDying() || b)
            return;

        b = true;
        if (sendCords.get()) {
                String coords = String.format("X: %d Y: %d Z: %d", Math.round(MC.player.position().x), Math.round(MC.player.position().y), Math.round(MC.player.position().z));
                Component reason = CAT_FORMAT.format("Death coordinates: {g}" + coords+"{reset}.");
                LOG.addEntry(this.name+": "+ reason.getString());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onOpenScreen(OpenScreenEvent event) {
        if (!(event.getScreen() instanceof DeathScreen)) return;
        if (MC == null) return;

        if (autoRespawn.get()) {

            MC.player.respawn();
            event.cancel();
        }
        b = false;
    }
}
