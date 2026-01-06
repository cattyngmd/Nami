package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

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
        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);

        if (freecamModule == null || !freecamModule.isEnabled()) {
            return originalPos;
        }

        float delta = MC.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        double interpolatedX =
                freecamModule.prevPos.x +
                        (freecamModule.pos.x - freecamModule.prevPos.x) * delta;

        double interpolatedZ =
                freecamModule.prevPos.z +
                        (freecamModule.pos.z - freecamModule.prevPos.z) * delta;

        return new BlockPos(
                (int) Math.floor(interpolatedX),
                originalPos.getY(),
                (int) Math.floor(interpolatedZ)
        );
    }
}
