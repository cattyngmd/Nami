package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void addEntity(Entity entity, CallbackInfo ci) {
        if (entity == null)
            return;

        EntitySpawnEvent ev = new EntitySpawnEvent(entity);
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled()) // you dont actually need this
            ci.cancel();
    }

    @Inject(method = "addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("HEAD"), cancellable = true)
    private void addDestroyBlockEffect(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        NoRenderModule nr = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (nr != null && nr.isEnabled() && nr.noBlockBreak.get())
            ci.cancel();
    }
    @Inject(method = "addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", at = @At("HEAD"), cancellable = true)
    private void addBreakingBlockEffect(BlockPos blockPos, Direction direction, CallbackInfo ci) {
        NoRenderModule nr = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (nr != null && nr.isEnabled() && nr.noBlockBreak.get())
            ci.cancel();
    }
}