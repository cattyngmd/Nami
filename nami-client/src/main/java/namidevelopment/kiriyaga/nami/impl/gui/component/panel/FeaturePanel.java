package namidevelopment.kiriyaga.nami.impl.gui.component.panel;

import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.api.model.setting.Setting;
import namidevelopment.kiriyaga.nami.impl.gui.base.BasePanel;

public class FeaturePanel extends BasePanel {

    public static final int HEIGHT = 13;

    private final Feature feature;

    public FeaturePanel(Feature feature) {
        this.feature = feature;
        this.height = HEIGHT;

        for (Setting<?> setting : feature.getSettings()) {
            if (setting == null) continue;
            //if (!setting.isShow()) continue;

            if (setting instanceof BoolSetting boolSetting) {
                addSubPanel(new BoolSettingPanel(boolSetting));
            }
        }
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
}
