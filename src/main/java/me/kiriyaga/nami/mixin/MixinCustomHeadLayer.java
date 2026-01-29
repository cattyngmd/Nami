package me.kiriyaga.nami.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.kiriyaga.nami.impl.feature.impl.visuals.NoRenderFeature;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(CustomHeadLayer.class)
public abstract class MixinCustomHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> {

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/SubmitNodeCollector;" + "ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, float f, float g, CallbackInfo ci) {
        NoRenderFeature nr = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);

       if (nr != null && nr.isEnabled() && nr.noArmor.get() && renderState.entityType == EntityType.PLAYER)
            ci.cancel();
    }
}