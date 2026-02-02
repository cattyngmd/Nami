package namidevelopment.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.FreecamFeature;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static namidevelopment.kiriyaga.nami.Nami.MC;
import static namidevelopment.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(ChunkBorderRenderer.class)
public abstract class MixinChunkBorderRenderer {

    @ModifyExpressionValue(
            method = "emitGizmos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;blockPosition()Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos nami$modifyEntityPos(BlockPos originalPos) {
        FreecamFeature freecamFeature = FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class);

        if (freecamFeature == null || !freecamFeature.isEnabled()) {
            return originalPos;
        }

        float delta = MC.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        double interpolatedX =
                freecamFeature.prevPos.x +
                        (freecamFeature.pos.x - freecamFeature.prevPos.x) * delta;

        double interpolatedZ =
                freecamFeature.prevPos.z +
                        (freecamFeature.pos.z - freecamFeature.prevPos.z) * delta;

        return new BlockPos(
                (int) Math.floor(interpolatedX),
                originalPos.getY(),
                (int) Math.floor(interpolatedZ)
        );
    }
}
