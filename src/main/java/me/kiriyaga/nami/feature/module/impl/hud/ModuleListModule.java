package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.text.Text;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ModuleListModule extends HudElementModule {

    public enum SortMode {
        ALPHABETICAL,
        DESCENDING,
        ASCENDING
    }

    private final List<TextElement> elements = new ArrayList<>();
    public final BoolSetting showDisplayName = addSetting(new BoolSetting("show display", true));
    public final EnumSetting<SortMode> sortMode = addSetting(new EnumSetting<>("sort", SortMode.DESCENDING));

    public ModuleListModule() {
        super("module list", "Shows enabled and drawn modules.", 52, 52, 50, 10);
    }

    @Override
    public List<TextElement> getTextElements() {
        elements.clear();

        List<Module> activeModules = new ArrayList<>(MODULE_MANAGER.getStorage().getAll().stream()
                .filter(Module::isEnabled)
                .filter(Module::isDrawn)
                .toList());

        if (activeModules.isEmpty()) return elements;

        switch (sortMode.get()) {
            case ALPHABETICAL -> activeModules.sort(Comparator.comparing(Module::getName, String::compareToIgnoreCase));
            case DESCENDING -> activeModules.sort((a, b) -> Integer.compare(
                    getTextWidth(b.getName() + (showDisplayName.get() && b.getDisplayInfo() != null && !b.getDisplayInfo().isEmpty() ? " " + b.getDisplayInfo() : "")),
                    getTextWidth(a.getName() + (showDisplayName.get() && a.getDisplayInfo() != null && !a.getDisplayInfo().isEmpty() ? " " + a.getDisplayInfo() : ""))
            ));
            case ASCENDING -> activeModules.sort(Comparator.comparingInt(a -> getTextWidth(a.getName() + (showDisplayName.get() && a.getDisplayInfo() != null && !a.getDisplayInfo().isEmpty() ? " " + a.getDisplayInfo() : ""))));
        }

        int yOffset = 0;
        int maxWidth = 0;

        for (Module module : activeModules) {
            String displayName = showDisplayName.get() ? module.getDisplayInfo() : null;
            Text text;

            if (displayName != null && !displayName.isEmpty()) {
                text = CAT_FORMAT.format("{bg}" + module.getName() + " {bgr}[{bw}" + displayName+"{bgr}]");
            } else {
                text = CAT_FORMAT.format("{bg}" + module.getName());
            }

            int textWidth = MC.textRenderer.getWidth(text);
            elements.add(new TextElement(text, 0, yOffset));

            maxWidth = Math.max(maxWidth, textWidth);
            yOffset += MC.textRenderer.fontHeight;
        }

        this.width = maxWidth;
        this.height = yOffset;

        return elements;
    }

    private int getTextWidth(String name) {
        return MC.textRenderer.getWidth(CAT_FORMAT.format(name));
    }
}
