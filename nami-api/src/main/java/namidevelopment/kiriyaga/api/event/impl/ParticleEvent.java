package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
import net.minecraft.core.particles.ParticleOptions;

public class ParticleEvent extends Event {
    private final ParticleOptions particle;


    public ParticleEvent(ParticleOptions particle) {
        this.particle = particle;
    }

    public ParticleOptions getParticle() {
        return particle;
    }
}
