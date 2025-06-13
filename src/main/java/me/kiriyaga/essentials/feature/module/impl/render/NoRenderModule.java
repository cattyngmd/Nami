package me.kiriyaga.essentials.feature.module.impl.render;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class NoRenderModule extends Module {



    private final BoolSetting noFire = addSetting(new BoolSetting("No Fire", true));
    private final BoolSetting noLiguid = addSetting(new BoolSetting("No Liquid", false));
    private final BoolSetting noWall = addSetting(new BoolSetting("No Wall", false));
    private final BoolSetting noVignette = addSetting(new BoolSetting("No Vignette", true));
    private final BoolSetting noTotem = addSetting(new BoolSetting("No Totem", true));
    private final BoolSetting noEating = addSetting(new BoolSetting("No Eating", true));
    private final BoolSetting noBossBar = addSetting(new BoolSetting("No Boss Bar", true));
    private final BoolSetting noPortal = addSetting(new BoolSetting("No Portal", true));
    private final BoolSetting noPotIcon = addSetting(new BoolSetting("No Pot Icon", true));
    private final BoolSetting noFog = addSetting(new BoolSetting("No Fog", true));
    private final BoolSetting noArmor = addSetting(new BoolSetting("No Armor", true));
    private final BoolSetting noNausea = addSetting(new BoolSetting("No Nausea", true));
    private final BoolSetting noPumpkin = addSetting(new BoolSetting("No Pumpkin", false));
    private final BoolSetting noPowderedSnow = addSetting(new BoolSetting("No Powdered Snow", false));

    public NoRenderModule() {
        super("No Render", "Prevent rendering certain overlays/effects", Category.RENDER, "norender");
    }

    @Override
    public void onEnable() {
        if (MINECRAFT.world != null)
            MINECRAFT.worldRenderer.reload();
    }
    @Override
    public void onDisable() {
        if (MINECRAFT.world != null)
            MINECRAFT.worldRenderer.reload();
    }

    public boolean isNoFire() {
        return noFire.get();
    }

    public boolean isNoLiguid() {
        return noLiguid.get();
    }

    public boolean isNoWall() {
        return noWall.get();
    }

    public boolean isNoVignette() {
        return noVignette.get();
    }

    public boolean isNoTotem() {
        return noTotem.get();
    }

    public boolean isNoEating() {
        return noEating.get();
    }

    public boolean isNoBossBar() {
        return noBossBar.get();
    }

    public boolean isNoPortal() {
        return noPortal.get();
    }

    public boolean isNoPotIcon() {
        return noPotIcon.get();
    }

    public boolean isNoFog() {
        return noFog.get();
    }

    public boolean isNoArmor() {
        return noArmor.get();
    }

    public boolean isNoNausea() {
        return noNausea.get();
    }

    public boolean isNoPumpkin() {
        return noPumpkin.get();
    }

    public boolean isNoPowderedSnow() {
        return noPowderedSnow.get();
    }

}
