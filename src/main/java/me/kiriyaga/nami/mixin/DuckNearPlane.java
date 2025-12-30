package me.kiriyaga.nami.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.phys.Vec3;

@Mixin(Camera.NearPlane.class)
public interface DuckNearPlane {

    @Accessor("forward")
    Vec3 getForward();
}
