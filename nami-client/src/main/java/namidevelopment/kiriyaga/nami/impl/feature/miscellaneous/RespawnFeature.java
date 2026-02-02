package namidevelopment.kiriyaga.nami.impl.feature.miscellaneous;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.OpenScreenEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class RespawnFeature extends Feature {

    public final BoolSetting sendCords = addSetting(new BoolSetting("LogCords", true));
    public final BoolSetting autoRespawn = addSetting(new BoolSetting("AutoRespawn", false));

    public RespawnFeature() {
        super("Respawn", "Death screen tweaks.", FeatureCategory.of("Miscellaneous"), "autorespawn");
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
