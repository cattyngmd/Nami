package me.kiriyaga.essentials.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("bodyYaw")
    void setBodyYaw(float yaw);

    @Accessor("headYaw")
    void setHeadYaw(float yaw);
}
