package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.setting.impl.KeyBindSetting;
import net.minecraft.client.option.Perspective;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class FreeLookModule extends Module {
    public float cameraYaw;
    public float cameraPitch;

    private Perspective previousPerspective;

    public DoubleSetting sensivity = addSetting(new DoubleSetting("sensivity", 5, 2, 15));
    public BoolSetting holdMode = addSetting(new BoolSetting("hold", true));


    public FreeLookModule() {
        super("free look", "Allows more rotation options in third person.", Category.RENDER, "freelook", "freelok", "third", "акуудщщл");
    }

    @Override
    public void onEnable() {
        this.getKeyBind().setHoldMode(holdMode.get());

        if (MINECRAFT.player == null) return;

        cameraYaw = MINECRAFT.player.getYaw();
        cameraPitch = MINECRAFT.player.getPitch();

        previousPerspective = MINECRAFT.options.getPerspective();
        if (previousPerspective != Perspective.THIRD_PERSON_BACK) {
            MINECRAFT.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDisable() {
        if (MINECRAFT.options.getPerspective() != previousPerspective) {
            MINECRAFT.options.setPerspective(previousPerspective);
        }
    }
}
