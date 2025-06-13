package me.kiriyaga.essentials.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("yaw")
    float getYaw();

    @Accessor("yaw")
    void setYaw(float yaw);
}
