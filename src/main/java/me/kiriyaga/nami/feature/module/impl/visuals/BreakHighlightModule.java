package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterModule
public class BreakHighlightModule extends Module {

    public final BoolSetting fill = addSetting(new BoolSetting("Fill", true));

    public BreakHighlightModule() {
        super("BreakHighlight", "Highlights block being broken.", ModuleCategory.of("Render"));
    }
}
