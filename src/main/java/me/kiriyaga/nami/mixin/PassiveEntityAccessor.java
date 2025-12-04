package me.kiriyaga.nami.mixin;

import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PassiveEntity.class)
public interface PassiveEntityAccessor {

    @Accessor("breedingAge")
    int breedingAge();

    @Accessor("breedingAge")
    void breedingAge(int age);
}
