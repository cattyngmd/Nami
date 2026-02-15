package namidevelopment.kiriyaga.nami.impl.gui.component.panel;

import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.Setting;
import namidevelopment.kiriyaga.nami.impl.gui.base.BasePanel;
import namidevelopment.kiriyaga.nami.impl.gui.component.panel.settings.BoolSettingPanel;

import java.util.ArrayList;
import java.util.List;

public class FeaturePanel extends BasePanel {

    public static final int HEIGHT = 13;

    private final Feature feature;
    private final List<Setting<?>> allSettings = new ArrayList<>();

    public FeaturePanel(Feature feature) {
        this.feature = feature;
        this.height = HEIGHT;

        for (Setting<?> setting : feature.getSettings()) {
            if (setting == null) continue;
            allSettings.add(setting);
        }
        refreshSettings();
    }

    @Override
    protected String getName() {
        return feature.getName();
    }

    @Override
    protected boolean isEnabled() {
        return feature.isEnabled();
    }

    @Override
    public void onLeftClick() {
        feature.toggle();
    }

    @Override
    public void onMiddleClick() {
        feature.setDrawn(!feature.isDrawn());
    }

    @Override
    public void onRightClick() {
        expanded = !expanded;
    }

    public Feature getFeature() {
        return feature;
    }

    public void refreshSettings() {
        getSubPanels().clear();

        for (Setting<?> setting : allSettings) {
            if (setting == null) continue;
            if (!setting.isShow()) continue;

            if (setting instanceof BoolSetting boolSetting) {
                addSubPanel(new BoolSettingPanel(boolSetting));
            }
        }
    }
}
