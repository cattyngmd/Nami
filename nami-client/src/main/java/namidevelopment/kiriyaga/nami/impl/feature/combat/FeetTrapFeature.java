package namidevelopment.kiriyaga.nami.impl.feature.combat;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.event.impl.Render3DEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.feature.combat.component.TrapComponent;

import net.minecraft.core.BlockPos;

import java.util.List;

import static namidevelopment.kiriyaga.api.NamiApi.FEATURE_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.MC;
import static namidevelopment.kiriyaga.api.util.BlockUtils.getSurround;

@RegisterFeature
public class FeetTrapFeature extends Feature {

    public final BoolSetting extension = addSetting(new BoolSetting("Extension", false));
    public final BoolSetting jumpDisable = addSetting(new BoolSetting("JumpDisable", false));

    private final TrapComponent trap;

    public FeetTrapFeature() {
        super("FeetTrap", "Places blocks around your feet.", FeatureCategory.of("Combat"), "feettrap");
        this.trap = new TrapComponent(this);
    }

    @Override
    public void onDisable() {
        trap.onDisable();
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        this.clearDisplayInfo();

        if (FEATURE_SERVICE.getStorage().getByClass(SelfTrapFeature.class).isEnabled())
            return;

        if (jumpDisable.get() && !MC.player.onGround()) {
            this.toggle();
            return;
        }

        List<BlockPos> targets = getTrapTargets();
        this.addDisplayInfo(targets.size() + "");
        trap.onTick(event, this, targets);
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        trap.onRender(event);
    }

    private List<BlockPos> getTrapTargets() {
        return getSurround(MC.player, 0, extension.get());
    }
}
