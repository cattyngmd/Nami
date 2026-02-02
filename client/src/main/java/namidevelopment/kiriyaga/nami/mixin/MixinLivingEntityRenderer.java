package namidevelopment.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import namidevelopment.kiriyaga.nami.impl.feature.impl.client.RotationsFeature;
import namidevelopment.kiriyaga.nami.impl.feature.impl.visuals.ChamsFeature;
import namidevelopment.kiriyaga.nami.mixininterface.IEntityRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static namidevelopment.kiriyaga.nami.Nami.*;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Unique private float oldHeadYaw;
    @Unique private float oldPrevHeadYaw;
    @Unique private float oldPitch;
    @Unique private float oldPrevPitch;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void extractRenderState(T livingEntity, S state, float f, CallbackInfo ci) {
        if (!(livingEntity instanceof LocalPlayer player)) return;
        if (player != MC.player) return;

        RotationsFeature rotations = FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class);
        if (!rotations.render.get()) return;

        oldHeadYaw = MC.player.yHeadRot;
        oldPrevHeadYaw = MC.player.yHeadRotO;
        oldPitch = MC.player.getXRot();
        oldPrevPitch = MC.player.xRotO;

        float serverYaw = ROTATION_SERVICE.getStateHandler().getServerYaw();
        float serverPitch = ROTATION_SERVICE.getStateHandler().getServerPitch();

        MC.player.yHeadRot = serverYaw; //TODO interpolate it
        MC.player.yHeadRotO = serverYaw;
        MC.player.setXRot(serverPitch);
        MC.player.xRotO = serverPitch;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractRenderState1(T livingEntity, S state, float f, CallbackInfo ci) {
        if (!(livingEntity instanceof LocalPlayer player)) return;
        if (player != MC.player) return;

        RotationsFeature rotations = FEATURE_SERVICE.getStorage().getByClass(RotationsFeature.class);
        if (!rotations.render.get()) return;

        MC.player.yHeadRot = oldHeadYaw;
        MC.player.yHeadRotO = oldPrevHeadYaw;
        MC.player.setXRot(oldPitch);
        MC.player.xRotO = oldPrevPitch;
    }

    @WrapWithCondition(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private <TState> boolean CrumblingOverlay(SubmitNodeCollector collector, Model<? super TState> model, TState state, PoseStack poseStack, RenderType renderType, int light, int overlay, int packedColor, TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumble) {
        ChamsFeature chams = FEATURE_SERVICE.getStorage().getByClass(ChamsFeature.class);
        if (!chams.isEnabled())
            return true;

        Entity entity = ((IEntityRenderState) state).getEntity();
        Color color = chams.getESPColor(entity);
        if (color == null) return true;
        collector.submitModel(model, state, poseStack, renderType, light, overlay, color.getRGB(), sprite, outlineColor, null);

        return false;
    }
}