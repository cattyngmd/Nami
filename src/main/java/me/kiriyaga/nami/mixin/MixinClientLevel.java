package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void addEntity(Entity entity, CallbackInfo ci) {
        if (entity == null)
            return;

        EntitySpawnEvent ev = new EntitySpawnEvent(entity);
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled()) // you dont actually need this
            ci.cancel();
    }
}