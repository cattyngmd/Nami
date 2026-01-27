package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.kiriyaga.nami.feature.module.impl.client.RotationsModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ChamsModule;
import me.kiriyaga.nami.mixininterface.IEntityRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static org.lwjgl.opengl.GL11C.GL_POLYGON_OFFSET_FILL;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "extractRenderState", at = @At("HEAD")) // todo: fix this
    private void updateRenderState(T livingEntity, LivingEntityRenderState livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntity instanceof LocalPlayer player && player == MC.player && ROTATION_MANAGER.getStateHandler().isRotating() && MODULE_MANAGER.getStorage().getByClass(RotationsModule.class).render.get()) {

            float prevYaw = livingEntityRenderState.bodyRot + livingEntityRenderState.yRot;
            float prevPitch = livingEntityRenderState.xRot;
            float targetYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
            float targetPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();
            float deltaYaw = targetYaw - prevYaw;
            if (deltaYaw > 180) deltaYaw -= 360;
            if (deltaYaw < -180) deltaYaw += 360;
            float smoothYaw = Mth.lerp(f, prevYaw, prevYaw + deltaYaw);
            float smoothPitch = Mth.lerp(f, prevPitch, targetPitch);

            livingEntityRenderState.yRot = Mth.wrapDegrees(smoothYaw - livingEntityRenderState.bodyRot);
            livingEntityRenderState.xRot = smoothPitch;
        }
    }


    @WrapWithCondition(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private <TState> boolean CrumblingOverlay(SubmitNodeCollector collector, Model<? super TState> model, TState state, PoseStack poseStack, RenderType renderType, int light, int overlay, int packedColor, TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumble) {
        ChamsModule chams = MODULE_MANAGER.getStorage().getByClass(ChamsModule.class);
        if (!chams.isEnabled())
            return true;

        Entity entity = ((IEntityRenderState) state).getEntity();
        Color color = chams.getESPColor(entity);
        if (color == null) return true;
        collector.submitModel(model, state, poseStack, renderType, light, overlay, color.getRGB(), sprite, outlineColor, null);

        return false;
    }
}