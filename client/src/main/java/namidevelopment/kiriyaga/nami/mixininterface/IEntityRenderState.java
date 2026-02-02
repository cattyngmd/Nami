package namidevelopment.kiriyaga.nami.mixininterface;

import net.minecraft.world.entity.Entity;

public interface IEntityRenderState {
    Entity getEntity();
    void setEntity(Entity entity);
}
