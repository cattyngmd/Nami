package namidevelopment.kiriyaga.nami.impl.feature.client;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.DoubleSetting;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;

@RegisterFeature
public class TargetFeature extends Feature {

    public final DoubleSetting targetRange = addSetting(new DoubleSetting("Range", 10.0, 4.0, 16.0));
    public final DoubleSetting minTicksExisted = addSetting(new DoubleSetting("Age", 12, 0.0, 20.0));
    public final BoolSetting targetPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting targetHostiles = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting targetNeutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting targetPassives = addSetting(new BoolSetting("Passives", false));
    public final BoolSetting targetPrijectiles = addSetting(new BoolSetting("Projectiles", true));
    public final EnumSetting<TargetPriority> priority = addSetting(new EnumSetting<>("Priority", TargetPriority.SMART));

    public enum TargetPriority {
        DISTANCE, HEALTH, SMART
    }

    public TargetFeature() {
        super("Target", "Allows you to configure target logic.", FeatureCategory.of("Client"), "entity", "entitySERVICE", "enity");
        if (!this.isEnabled())
            this.toggle();
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
