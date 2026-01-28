package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ModuleListModule extends HudElementModule {

    public enum SortMode {
        ALPHABETICAL,
        DESCENDING,
        ASCENDING
    }

    private final List<TextElement> elements = new ArrayList<>();
    private final Map<String, ModuleAnimationState> animationStates = new ConcurrentHashMap<>();
    private long lastUpdateTime = System.currentTimeMillis();
    private int cachedWidth = 0;
    private int cachedHeight = 0;

    public final BoolSetting showDisplayName = addSetting(new BoolSetting("ShowDisplay", true));
    public final EnumSetting<SortMode> sortMode = addSetting(new EnumSetting<>("Sort", SortMode.DESCENDING));

    public ModuleListModule() {
        super("ModuleList", "Shows enabled and drawn modules.", 0, 0, 50, 10);
    }

    private static class ModuleAnimationState {
        public float progress = 0f;
        public long startTime;
        public int textWidth;
    }

    @Override
    public Rectangle getBoundingBox() {
        return new Rectangle(0, 0, cachedWidth, cachedHeight);
    }

    @Override
    public List<TextElement> getTextElements() {
        long currentTime = System.currentTimeMillis();
        elements.clear();

        List<Module> activeModules = new ArrayList<>(MODULE_MANAGER.getStorage().getAll().stream().filter(Module::isEnabled).filter(Module::isDrawn).toList());

        animationStates.keySet().removeIf(name -> activeModules.stream().noneMatch(module -> module.getName().equals(name)));

        for (Module module : activeModules) {
            String name = module.getName();
            if (!animationStates.containsKey(name)) {
                ModuleAnimationState state = new ModuleAnimationState();
                state.startTime = currentTime;

                List<Component> displayInfos = showDisplayName.get() ? module.getDisplayInfo() : null;

                String rawText;

                if (displayInfos != null && !displayInfos.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(module.getName()).append(" [");

                    for (int i = 0; i < displayInfos.size(); i++) {
                        Component info = displayInfos.get(i);
                        if (info == null) continue;

                        sb.append("{bw}").append(info.getString());

                        if (i < displayInfos.size() - 1) {
                            sb.append("{bg},");
                        }
                    }
                    sb.append("{bg}]");
                    rawText = sb.toString();

                } else {
                    rawText = module.getName();
                }
                Component formattedText = CAT_FORMAT.format("{bg}" + rawText);
                state.textWidth = FONT_MANAGER.getWidth(formattedText);
                animationStates.put(name, state);
            }
        }

        for (ModuleAnimationState state : animationStates.values()) {
            long elapsed = currentTime - state.startTime;
            float normalizedTime = Math.min(elapsed / 150f, 1f);

            if (normalizedTime < 0.5f) {
                state.progress = 2 * normalizedTime * normalizedTime;
            } else {
                float t = normalizedTime * 2 - 2;
                state.progress = -0.5f * (t * t - 2);
            }

            if (elapsed >= 150) {
                state.progress = 1f;
            }
        }

        if (activeModules.isEmpty()) {
            return elements;
        }

        List<ModuleTextInfo> moduleTexts = new ArrayList<>();
        for (Module module : activeModules) {
            List<Component> displayInfos = showDisplayName.get() ? module.getDisplayInfo() : null;
            String rawText;
            String formattedTextStr;

            if (displayInfos != null && !displayInfos.isEmpty()) {
                StringBuilder rawSb = new StringBuilder();
                rawSb.append(module.getName()).append(" [");
                StringBuilder formattedSb = new StringBuilder();
                formattedSb.append("{bg}").append(module.getName()).append(" {bg}[");

                for (int i = 0; i < displayInfos.size(); i++) {
                    Component info = displayInfos.get(i);
                    if (info == null) continue;

                    rawSb.append(info.getString());
                    formattedSb.append("{bw}").append(info.getString());

                    if (i < displayInfos.size() - 1) {
                        rawSb.append(",");
                        formattedSb.append("{bg},");
                    }
                }

                rawSb.append("]");
                formattedSb.append("{bg}]");

                rawText = rawSb.toString();
                formattedTextStr = formattedSb.toString();

            } else {
                rawText = module.getName();
                formattedTextStr = "{bg}" + module.getName();
            }
            Component formattedText = CAT_FORMAT.format(formattedTextStr);
            int width = FONT_MANAGER.getWidth(formattedText);
            moduleTexts.add(new ModuleTextInfo(module, formattedText, rawText, width));
        }

        switch (sortMode.get()) {
            case ALPHABETICAL -> moduleTexts.sort(Comparator.comparing(info -> info.rawText, String::compareToIgnoreCase));
            case DESCENDING -> moduleTexts.sort((a, b) -> Integer.compare(b.width, a.width));
            case ASCENDING -> moduleTexts.sort(Comparator.comparingInt(a -> a.width));
        }

        int yOffset = 0;
        int maxWidth = 0;

        for (ModuleTextInfo info : moduleTexts) {
            maxWidth = Math.max(maxWidth, info.width);
            yOffset += FONT_MANAGER.getHeight();
        }

        cachedWidth = maxWidth;
        cachedHeight = yOffset;
        this.width = cachedWidth;
        this.height = cachedHeight;

        yOffset = 0;
        for (ModuleTextInfo info : moduleTexts) {
            String moduleName = info.module.getName();
            ModuleAnimationState state = animationStates.get(moduleName);

            if (state == null || state.progress <= 0f) {
                yOffset += FONT_MANAGER.getHeight();
                continue;
            }

            Component text = info.formattedText;

            int animatedOffsetX = 0;
            switch (alignment.get()) {
                case LEFT:
                    animatedOffsetX = (int) ((state.progress - 1) * state.textWidth);
                    break;
                case CENTER:
                    animatedOffsetX = (int) ((1 - state.progress) * -state.textWidth);
                    break;
                case RIGHT:
                    animatedOffsetX = (int) ((state.progress - 1) * state.textWidth);
                    break;
            }

            elements.add(new TextElement(text, animatedOffsetX, yOffset));
            yOffset += FONT_MANAGER.getHeight();
        }

        lastUpdateTime = currentTime;
        return elements;
    }

    @Override
    public int getRenderXForElement(TextElement element) {
        int baseX = getRenderX();
        int lineWidth = FONT_MANAGER.getWidth(element.text());

        return switch (alignment.get()) {
            case LEFT -> baseX + element.offsetX();
            case CENTER -> baseX + (width - lineWidth) / 2 + element.offsetX();
            case RIGHT -> baseX + width - lineWidth - element.offsetX();
        };
    }

    private int getTextWidth(String text) {
        return FONT_MANAGER.getWidth(CAT_FORMAT.format("{bg}" + text));
    }

    private static class ModuleTextInfo {
        public final Module module;
        public final Component formattedText;
        public final String rawText;
        public final int width;

        public ModuleTextInfo(Module module, Component formattedText, String rawText, int width) {
            this.module = module;
            this.formattedText = formattedText;
            this.rawText = rawText;
            this.width = width;
        }
    }
}