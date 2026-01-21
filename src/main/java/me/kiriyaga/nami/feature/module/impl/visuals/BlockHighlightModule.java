package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterModule
public class BlockHighlightModule extends Module {

    public final BoolSetting fill = addSetting(new BoolSetting("Fill", true));

    public BlockHighlightModule() {
        super("BlockHighlight", "Highlights block you look at.", ModuleCategory.of("Render"));
    }

    @SubscribeEvent
    public void onRender3dEvent(Render3DEvent event) {
        if (MC.hitResult != null && MC.hitResult instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() == HitResult.Type.ENTITY)
                return;

            RenderUtil.drawBlockPosLines(MC.level, blockHitResult.getBlockPos(), MC.level.getBlockState(blockHitResult.getBlockPos()), MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor(), fill.get(), true, 1.5f);
        }
    }
}
