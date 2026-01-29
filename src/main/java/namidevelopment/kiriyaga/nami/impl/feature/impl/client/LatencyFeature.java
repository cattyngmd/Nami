package namidevelopment.kiriyaga.nami.impl.feature.impl.client;

import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

@RegisterFeature
public class LatencyFeature extends Feature {

    public enum mode {
        OLD,
        NEW,
        OFF
    }

    public final EnumSetting<mode> fastLatencyMode = addSetting(new EnumSetting<>("Mode", mode.NEW));
    public final IntSetting smoothingStrength = addSetting(new IntSetting("Smooth", 10, 1, 50));
    public final IntSetting unstableConnectionTimeout = addSetting(new IntSetting("Unstable", 3, 1, 60));
    public final IntSetting keepAliveInterval = addSetting(new IntSetting("Interval", 900, 250, 2500));

    public LatencyFeature() {
        super("Latency", "Defines how ping should be calculated.", FeatureCategory.of("Client"), "ping", "SERVICE", "managr", "png");
        if (!this.isEnabled())
            this.toggle();

        smoothingStrength.setShowCondition(() -> fastLatencyMode.get() == mode.OLD);
        unstableConnectionTimeout.setShowCondition(() -> fastLatencyMode.get() != mode.OFF);
        keepAliveInterval.setShowCondition(() -> fastLatencyMode.get() == mode.OLD);
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
