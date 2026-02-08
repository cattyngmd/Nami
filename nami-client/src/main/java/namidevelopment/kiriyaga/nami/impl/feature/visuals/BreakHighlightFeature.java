package namidevelopment.kiriyaga.nami.impl.feature.visuals;

import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.core.breakprediction.PlayerBreakState;
import namidevelopment.kiriyaga.api.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;

import static namidevelopment.kiriyaga.api.NamiApi.BREAKPREDICT_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.MC;

@RegisterFeature
public class BreakHighlightFeature extends Feature {

    public final BoolSetting fill = addSetting(new BoolSetting("Fill", true));

    public BreakHighlightFeature() {
        super("BreakHighlight", "Highlights block being broken.", FeatureCategory.of("Render"));
    }

    @SubscribeEvent
    public void onRender3DEvent(Render3DEvent event) {
        if (MC.level == null || MC.player == null || BREAKPREDICT_SERVICE == null)
            return;

        for (PlayerBreakState state : BREAKPREDICT_SERVICE.all()) {
            state.render(event, fill.get());
        }
    }
}
