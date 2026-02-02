package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.event.impl.ParticleEvent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.nami.Nami.EVENT_SERVICE;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine {
    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void onAddParticle(ParticleOptions particle, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> ci) {
        ParticleEvent ev = new ParticleEvent(particle);

        EVENT_SERVICE.post(ev);

        if (ev.isCancelled())
            ci.cancel();
    }
}