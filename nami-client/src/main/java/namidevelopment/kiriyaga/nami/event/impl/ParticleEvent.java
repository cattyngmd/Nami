package namidevelopment.kiriyaga.nami.event.impl;

import namidevelopment.kiriyaga.nami.event.Event;
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
