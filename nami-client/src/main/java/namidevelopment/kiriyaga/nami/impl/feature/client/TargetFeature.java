package namidevelopment.kiriyaga.nami.impl.feature.client;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.contract.feature.TargetFeatureConfig;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.DoubleSetting;
import namidevelopment.kiriyaga.api.model.setting.EnumSetting;

@RegisterFeature
public class TargetFeature extends Feature implements TargetFeatureConfig {

    public final DoubleSetting targetRange = addSetting(new DoubleSetting("Range", 10.0, 4.0, 16.0));
    public final DoubleSetting minTicksExisted = addSetting(new DoubleSetting("Age", 12, 0.0, 20.0));
    public final BoolSetting targetPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting targetHostiles = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting targetNeutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting targetPassives = addSetting(new BoolSetting("Passives", false));
    public final BoolSetting targetPrijectiles = addSetting(new BoolSetting("Projectiles", true));
    public final EnumSetting<TargetPriority> priority = addSetting(new EnumSetting<>("Priority", TargetPriority.SMART));

    @Override
    public double getTargetRange() {
        return targetRange.get();
    }

    @Override
    public double getMinTicksExisted() {
        return minTicksExisted.get();
    }

    @Override
    public boolean targetPlayers() {
        return targetPlayers.get();
    }

    @Override
    public boolean targetHostiles() {
        return targetHostiles.get();
    }

    @Override
    public boolean targetNeutrals() {
        return targetNeutrals.get();
    }

    @Override
    public boolean targetPassives() {
        return targetPassives.get();
    }

    @Override
    public boolean targetProjectiles() {
        return targetPrijectiles.get();
    }

    @Override
    public TargetFeatureConfig.TargetPriority getPriority() {
        return priority.get();
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
