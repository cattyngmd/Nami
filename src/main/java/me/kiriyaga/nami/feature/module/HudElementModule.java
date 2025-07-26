package me.kiriyaga.nami.feature.module;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.MC;

public abstract class HudElementModule extends Module {

    public final IntSetting x;
    public final IntSetting y;
    public final EnumSetting<HudAlignment> alignment;

    public int width;
    public int height;
    public boolean skipAnimation;

    public HudElementModule(String name, String description, int defaultX, int defaultY, int width, int height) {
        super(name, description, ModuleCategory.of("hud"));

        this.width = width;
        this.height = height;

        this.x = addSetting(new IntSetting("x", defaultX, 0, 4321));
        this.y = addSetting(new IntSetting("y", defaultY, 0, 4321));
        this.alignment = addSetting(new EnumSetting<>("alignment", HudAlignment.LEFT));
    }

    public int getRenderX() {
        int screenWidth = MC.getWindow().getScaledWidth();
        int posX = x.get();
        int constrainedX = posX;

        // totallen shitcode
        switch (alignment.get()) {
            case LEFT:
                constrainedX = Math.min(Math.max(posX, 0), screenWidth - width);
                return constrainedX;

            case CENTER:
                constrainedX = Math.min(Math.max(posX, width / 2), screenWidth - width / 2);
                return constrainedX - width / 2;

            case RIGHT:
                constrainedX = Math.min(Math.max(posX, width), screenWidth);
                return constrainedX - width;

            default:
                return posX;
        }
    }

    public int getRenderY() {
        int screenHeight = MC.getWindow().getScaledHeight();
        int posY = y.get();

        if (posY + height > screenHeight) {
            return screenHeight - height;
        }
        if (posY < 0) return 0;

        return posY;
    }

    public abstract Text getDisplayText();
}
