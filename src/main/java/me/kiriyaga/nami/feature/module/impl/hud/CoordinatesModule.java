package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class CoordinatesModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));
    public final BoolSetting altCords = addSetting(new BoolSetting("alt coordinates", true));

    public CoordinatesModule() {
        super("coordinates", "Displays player coordinates.", 52, 52, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        if (MC.player == null || MC.world == null) {
            return CAT_FORMAT.format("{bg}XYZ: {bw}NaN");
        }

        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        boolean isNether = MC.world.getRegistryKey() == World.NETHER;
        boolean isOverworld = MC.world.getRegistryKey() == World.OVERWORLD;

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

        width = MC.textRenderer.getWidth(formatted.replaceAll("\\{.*?}", ""));
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format(formatted);
    }

    private String formatNumber(double val) {
        double rounded = Math.round(val * 10.0) / 10.0;
        return String.format("%.1f", rounded).replace(',', '.');
    }
}
