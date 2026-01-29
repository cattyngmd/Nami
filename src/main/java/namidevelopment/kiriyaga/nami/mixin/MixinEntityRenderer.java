package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.impl.feature.impl.visuals.NametagsFeature;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "getNameTag", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(T entity, CallbackInfoReturnable<Component> cir) {
        NametagsFeature nametagsFeature = FEATURE_SERVICE.getStorage() != null
                ? FEATURE_SERVICE.getStorage().getByClass(NametagsFeature.class)
                : null;

        if (nametagsFeature != null && nametagsFeature.isEnabled()) {
            cir.setReturnValue(null);
            return;
        }
    }
}
