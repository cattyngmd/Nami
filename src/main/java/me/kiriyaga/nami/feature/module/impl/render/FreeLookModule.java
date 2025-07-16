package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import net.minecraft.client.option.Perspective;

import static me.kiriyaga.nami.Nami.MC;

public class FreeLookModule extends Module {
    public float cameraYaw;
    public float cameraPitch;

    private Perspective previousPerspective;

    public DoubleSetting sensivity = addSetting(new DoubleSetting("sensivity", 5, 2, 15));
    public BoolSetting holdMode = addSetting(new BoolSetting("hold", true));


    public FreeLookModule() {
        super("free look", "Look around freely without moving your real yaw/pitch.", Category.visuals, "freelook", "freelok", "third", "акуудщщл");
    }

    @Override
    public void onEnable() {
        this.getKeyBind().setHoldMode(holdMode.get());

        if (MC.player == null) return;

        cameraYaw = MC.player.getYaw();
        cameraPitch = MC.player.getPitch();

        previousPerspective = MC.options.getPerspective();
        if (previousPerspective != Perspective.THIRD_PERSON_BACK) {
            MC.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDisable() {
        if (MC.options.getPerspective() != previousPerspective) {
            MC.options.setPerspective(previousPerspective);
        }
    }
}
