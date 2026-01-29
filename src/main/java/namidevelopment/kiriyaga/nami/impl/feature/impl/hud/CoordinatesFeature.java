package namidevelopment.kiriyaga.nami.impl.feature.impl.hud;

import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class CoordinatesFeature extends HudElementFeature {

    public enum LayoutMode {
        HORIZONTAL, VERTICAL
    }

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));
    public final BoolSetting altCords = addSetting(new BoolSetting("AltCoordinates", true));
    public final BoolSetting precise = addSetting(new BoolSetting("Precise", false));
    public final EnumSetting<LayoutMode> layout = addSetting(new EnumSetting<>("Layout", LayoutMode.HORIZONTAL));

    public CoordinatesFeature() {
        super("Coordinates", "Displays player coordinates.", 0, 0, 100, 30);
    }

    @Override
    public Component getDisplayText() {
        if (layout.get() == LayoutMode.VERTICAL) return null; // use getTextElements()

        if (MC.player == null || MC.level == null) {
            return CAT_FORMAT.format("{bg}XYZ: {bw}NaN");
        }

        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        boolean isNether = MC.level.dimension() == Level.NETHER;
        boolean isOverworld = MC.level.dimension() == Level.OVERWORLD;

        double xAlt = isNether ? x * 8 : x / 8;
        double zAlt = isNether ? z * 8 : z / 8;

        String formatted = "";

        if (displayLabel.get()) {
            formatted += "{bg}XYZ: ";
        }

        formatted += "{bw}" + formatNumber(x) + "{bg}, {bw}"
                + formatNumber(y) + "{bg}, {bw}"
                + formatNumber(z);

        if ((isOverworld || isNether) && altCords.get()) {
            formatted += " {bg}[{bw}" + formatNumber(xAlt) + "{bg}, {bw}" + formatNumber(zAlt) + "{bg}]";
        }

        width = FONT_SERVICE.getWidth(formatted.replaceAll("\\{.*?}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(formatted);
    }

    @Override
    public List<TextElement> getTextElements() {
        if (layout.get() != LayoutMode.VERTICAL) {
            return super.getTextElements(); // fallback to getDisplayText()
        }

        List<TextElement> lines = new ArrayList<>();

        if (MC.player == null || MC.level == null) {
            lines.add(new TextElement(CAT_FORMAT.format("{bg}XYZ: {bw}NaN"), 0, 0));
            return lines;
        }

        double x = MC.getCameraEntity().getX();
        double y = MC.getCameraEntity().getY();
        double z = MC.getCameraEntity().getZ();

        boolean isNether = MC.level.dimension() == Level.NETHER;
        boolean isOverworld = MC.level.dimension() == Level.OVERWORLD;

        double xAlt = isNether ? x * 8 : x / 8;
        double zAlt = isNether ? z * 8 : z / 8;

        int lineHeight = FONT_SERVICE.getHeight() + 1;
        int offsetY = 0;

        String fx = "{bg}X: {bw}" + formatNumber(x);
        if ((isOverworld || isNether) && altCords.get()) {
            fx += " {bg}[{bw}" + formatNumber(xAlt) + "{bg}]";
        }

        lines.add(new TextElement(CAT_FORMAT.format(fx), 0, offsetY));
        offsetY += lineHeight;

        lines.add(new TextElement(CAT_FORMAT.format("{bg}Y: {bw}" + formatNumber(y)), 0, offsetY));
        offsetY += lineHeight;

        String fz = "{bg}Z: {bw}" + formatNumber(z);
        if ((isOverworld || isNether) && altCords.get()) {
            fz += " {bg}[{bw}" + formatNumber(zAlt) + "{bg}]";
        }

        lines.add(new TextElement(CAT_FORMAT.format(fz), 0, offsetY));
        offsetY += lineHeight;

        int maxWidth = lines.stream()
                .mapToInt(te -> FONT_SERVICE.getWidth(te.text().getString().replaceAll("\\{.*?}", "")))
                .max().orElse(0);

        width = maxWidth;
        height = offsetY;

        return lines;
    }

    private String formatNumber(double val) {
        return String.format(precise.get() ? "%.3f" : "%.1f", val).replace(',', '.');
    }
}