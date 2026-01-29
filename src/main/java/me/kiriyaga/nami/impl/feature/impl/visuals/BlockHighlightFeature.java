package me.kiriyaga.nami.impl.feature.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.feature.FeatureCategory;
import me.kiriyaga.nami.impl.feature.RegisterFeature;
import me.kiriyaga.nami.impl.feature.impl.client.ColorFeature;
import me.kiriyaga.nami.impl.setting.impl.BoolSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

@RegisterFeature
public class BlockHighlightFeature extends Feature {

    public final BoolSetting fill = addSetting(new BoolSetting("Fill", true));

    public BlockHighlightFeature() {
        super("BlockHighlight", "Highlights block you look at.", FeatureCategory.of("Render"));
    }

    @SubscribeEvent
    public void onRender3dEvent(Render3DEvent event) {
        if (MC.hitResult != null && MC.hitResult instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() == HitResult.Type.ENTITY)
                return;

            RenderUtil.drawBlockPosLines(MC.level, blockHitResult.getBlockPos(), MC.level.getBlockState(blockHitResult.getBlockPos()), FEATURE_SERVICE.getStorage().getByClass(ColorFeature.class).getStyledGlobalColor(), fill.get(), true, 1.5f);
        }
    }
}
