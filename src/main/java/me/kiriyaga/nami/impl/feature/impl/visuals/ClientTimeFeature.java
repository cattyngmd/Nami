package me.kiriyaga.nami.impl.feature.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.WorldTimeUpdateEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;

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