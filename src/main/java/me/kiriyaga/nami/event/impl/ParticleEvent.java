package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.BlockPos;

public class ParticleEvent extends Event {
    private final ParticleOptions particle;


    public ParticleEvent(ParticleOptions particle) {
        this.particle = particle;
    }

    public ParticleOptions getParticle() {
        return particle;
    }
}
