package me.kiriyaga.nami.feature.module.impl.render;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class NoRenderModule extends Module {



    private final BoolSetting noFire = addSetting(new BoolSetting("fire", true));
    private final BoolSetting noBackground = addSetting(new BoolSetting("background", true));
    private final BoolSetting noLiguid = addSetting(new BoolSetting("liquid", false));
    private final BoolSetting noWall = addSetting(new BoolSetting("wall", false));
    private final BoolSetting noVignette = addSetting(new BoolSetting("vignette", true));
    private final BoolSetting noTotem = addSetting(new BoolSetting("totem", true));
    private final BoolSetting noBossBar = addSetting(new BoolSetting("boss", true));
    private final BoolSetting noPortal = addSetting(new BoolSetting("portal", true));
    private final BoolSetting noPotIcon = addSetting(new BoolSetting("pot", true));
    private final BoolSetting noFog = addSetting(new BoolSetting("fog", true));
    private final BoolSetting noArmor = addSetting(new BoolSetting("armor", true));
    private final BoolSetting noNausea = addSetting(new BoolSetting("nausea", true));
    private final BoolSetting noPumpkin = addSetting(new BoolSetting("pumpkin", false));
    private final BoolSetting noPowderedSnow = addSetting(new BoolSetting("powdered snow", false));

    public NoRenderModule() {
        super("no render", "Prevent rendering certain overlays/effects.", Category.visuals, "norender");
        noFire.setOnChanged(this::reloadRenderer);
        noBackground.setOnChanged(this::reloadRenderer);
        noLiguid.setOnChanged(this::reloadRenderer);
        noVignette.setOnChanged(this::reloadRenderer);
        noPortal.setOnChanged(this::reloadRenderer);
        noFog.setOnChanged(this::reloadRenderer);
        noPumpkin.setOnChanged(this::reloadRenderer);
        noPowderedSnow.setOnChanged(this::reloadRenderer);
    }

    private void reloadRenderer() {
        if (MINECRAFT.world != null) {
            MINECRAFT.worldRenderer.reload();
        }
    }

    @Override
    public void onEnable() {
        reloadRenderer();
    }

    @Override
    public void onDisable() {
        reloadRenderer();
    }

    public boolean isNoFire() {
        return noFire.get();
    }

    public boolean isNoBackground() {
        if (MINECRAFT.world == null)
            return false;

        return noBackground.get();
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
