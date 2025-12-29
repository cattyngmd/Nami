package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.ParticleEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine {
    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void onAddParticle(ParticleOptions particle, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> ci) {
        ParticleEvent ev = new ParticleEvent(particle);

        EVENT_MANAGER.post(ev);

        if (ev.isCancelled())
            ci.cancel();
    }
}