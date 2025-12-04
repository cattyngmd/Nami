package me.kiriyaga.nami.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimalEntity.class)
public interface AnimalEntityAccessor {

    @Accessor("loveTicks")
    int loveTicks();

    @Accessor("loveTicks")
    void loveTicks(int ticks);
}
