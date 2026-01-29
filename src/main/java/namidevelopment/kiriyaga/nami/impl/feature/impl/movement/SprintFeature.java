package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.client.player.LocalPlayer;

import static namidevelopment.kiriyaga.nami.Nami.MC;
// some crazy shit happened here
@RegisterFeature
public class SprintFeature extends Feature {

    private final BoolSetting inLiquid = addSetting(new BoolSetting("InLiquid", true));
/*
    public final BoolSetting twobtwot = addSetting(new BoolSetting("2b2t", false));
*/

    private int shouldSprintTicks = 0; // yes sorry

    public SprintFeature() {
        super("Sprint", "Automatically makes you sprint while moving.", FeatureCategory.of("Movement"));
    }

    public void stopSprinting(int i) {
        this.shouldSprintTicks = i;
    }

    private boolean shouldForceNoSprint() {
        return shouldSprintTicks > 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreTickEvent(PreTickEvent event) {
        LocalPlayer player = MC.player;
        if (player == null) return;

        if (shouldSprintTicks > 0) {
            shouldSprintTicks--;
        }

        if (!inLiquid.get() && (player.isUnderWater() || player.isInWater()))
            return;

        if (shouldForceNoSprint()) {
            player.setSprinting(false);
            return;
        }

        boolean canSprint = player.zza > 0 && !player.isPassenger();

        if (canSprint) {
            player.setSprinting(true);
        } else {
            player.setSprinting(false);
        }
    }
}
