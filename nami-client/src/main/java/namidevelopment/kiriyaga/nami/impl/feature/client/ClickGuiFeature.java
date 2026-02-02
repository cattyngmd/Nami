package namidevelopment.kiriyaga.nami.impl.feature.client;

import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class ClickGuiFeature extends Feature {

    public final DoubleSetting scale = addSetting(new DoubleSetting("Scale", 1.00, 0.50, 1.50));
    public final BoolSetting lines = addSetting(new BoolSetting("Lines", true));
    public final BoolSetting descriptions = addSetting(new BoolSetting("Descriptions", true));
    public final IntSetting guiAlpha = addSetting(new IntSetting("UIAlpha", 70, 0, 255));
    public final BoolSetting fade = addSetting(new BoolSetting("Fade", true));
    public final BoolSetting blur = addSetting(new BoolSetting("Blur", false));
    public final BoolSetting panels = addSetting(new BoolSetting("Panels", false));
    public final BoolSetting FeatureFill = addSetting(new BoolSetting("FeatureFill", true));
    public final BoolSetting coloredText = addSetting(new BoolSetting("ColoredText", false));
    public final BoolSetting background = addSetting(new BoolSetting("Background", true));
    public final BoolSetting FeatureChatFeedback = addSetting(new BoolSetting("FeatureChatFeedback", true));

    public ClickGuiFeature() {
        super("ClickGui", "Opens client UI.", FeatureCategory.of("Client"), "clickgui","click", "gui", "menu", "clckgui");
        this.keyBind.setDefaultKey(80);
    }

    @Override
    public void onEnable(){
        if (MC == null || MC.mouseHandler == null)
            return;

        NAVIGATE_PANEL.resetActive();
        CLICK_GUI_SCREEN.scale = this.scale.get().floatValue(); // bad
        CLICK_GUI_SCREEN.setPreviousScreen(MC.screen);

        MC.setScreen(CLICK_GUI_SCREEN);
        this.toggle();
    }
}
