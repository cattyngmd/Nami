package namidevelopment.kiriyaga.api.event.impl;

import namidevelopment.kiriyaga.api.event.Event;
import net.minecraft.world.entity.LivingEntity;

public class EntityDeathEvent extends Event {

    private final LivingEntity livingEntity;

    public EntityDeathEvent(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }
}