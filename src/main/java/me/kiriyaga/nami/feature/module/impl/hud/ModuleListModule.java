package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.text.Text;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ModuleListModule extends HudElementModule {

    public enum SortMode {
        alphabetical,
        descending,
        ascending
    }

    private final List<TextElement> elements = new ArrayList<>();
    public final EnumSetting<SortMode> sortMode = addSetting(new EnumSetting<>("sort", SortMode.descending));

    public ModuleListModule() {
        super("module list", "Shows enabled and drawn modules.", 2, 2, 50, 10);
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
            case alphabetical -> activeModules.sort(Comparator.comparing(Module::getName, String::compareToIgnoreCase));
            case descending -> activeModules.sort((a, b) -> Integer.compare(
                    getTextWidth(b.getName()),
                    getTextWidth(a.getName())
            ));
            case ascending -> activeModules.sort(Comparator.comparingInt(a -> getTextWidth(a.getName())));
        }

        int yOffset = 0;
        int maxWidth = 0;

        for (Module module : activeModules) {
            Text text = CAT_FORMAT.format("{bg}" + module.getName());
            int textWidth = MC.textRenderer.getWidth(text);
            elements.add(new TextElement(text, 0, yOffset));

            maxWidth = Math.max(maxWidth, textWidth);
            yOffset += MC.textRenderer.fontHeight + 1;
        }

        this.width = maxWidth;
        this.height = yOffset;

        return elements;
    }

    private int getTextWidth(String name) {
        return MC.textRenderer.getWidth(CAT_FORMAT.format("{bg}" + name));
    }
}
