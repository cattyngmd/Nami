package namidevelopment.kiriyaga.nami.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import namidevelopment.kiriyaga.nami.impl.feature.visuals.NoRenderFeature;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.nami.Nami.FEATURE_SERVICE;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;" + "Lnet/minecraft/client/renderer/SubmitNodeCollector;" + "ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",at = @At("HEAD"), cancellable = true)
    private void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, float f, float g, CallbackInfo ci) {
        NoRenderFeature nr = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);

        if (nr != null && nr.isEnabled() && nr.noArmor.get() && renderState.entityType == EntityType.PLAYER)
            ci.cancel();
    }
}
