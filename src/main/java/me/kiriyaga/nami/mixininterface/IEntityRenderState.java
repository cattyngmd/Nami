package me.kiriyaga.nami.mixininterface;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IEntityRenderState {
    Entity getEntity();
    void setEntity(Entity entity);
}
