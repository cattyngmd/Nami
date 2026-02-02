package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.event.impl.WorldTimeUpdateEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class ClientTimeFeature extends Feature {
    public final IntSetting value = addSetting(new IntSetting("Time", 25000, 0, 25000));

    public ClientTimeFeature() {
        super("ClientTime", "Sets game time client side.", FeatureCategory.of("Render"));
    }

    @SubscribeEvent
    private void onPacketReceiveEvent(WorldTimeUpdateEvent event) {
        event.cancel();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onPreTickEvent(PreTickEvent event) {
        if (MC.level == null || MC.player == null)
            return;

        MC.level.getLevelData().setDayTime((long)value.get());
    }
}