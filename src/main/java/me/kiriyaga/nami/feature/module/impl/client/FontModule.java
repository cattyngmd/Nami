package me.kiriyaga.nami.feature.module.impl.client;

import com.mojang.blaze3d.textures.FilterMode;
import me.kiriyaga.nami.core.font.FontType;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class FontModule extends Module {

    public final EnumSetting<FontType> fontType = addSetting(new EnumSetting<>("Font", FontType.VERDANAPRO));
    public final IntSetting shadowDarken = addSetting(new IntSetting("ShadowDarken", 75, 60, 100));
    public final BoolSetting global = addSetting(new BoolSetting("Global", false));
    public final EnumSetting<FilterMode> filterMode = addSetting(new EnumSetting<>("Filter", FilterMode.NEAREST));
    public final IntSetting anisotropy = addSetting(new IntSetting("Anisotropy", 8, 1, 16));
    public final DoubleSetting maxLOD = addSetting(new DoubleSetting("MaxLOD", 10.00, -1, 25));
    public final IntSetting glyphSize = addSetting(new IntSetting("Size", 9, 6, 24));
    public final IntSetting oversample = addSetting(new IntSetting("Oversample", 2, 2, 8));

    public FontModule() {
        super("Font", "Custom font renderer.", ModuleCategory.of("Client"), "f", "customfont");

        global.setShow(false);
        filterMode.setShow(false);
        anisotropy.setShow(false);
        maxLOD.setShow(false);
    }
}
