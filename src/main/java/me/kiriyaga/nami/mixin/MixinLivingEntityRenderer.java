package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.kiriyaga.nami.feature.module.impl.client.RotationsModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ChamsModule;
import me.kiriyaga.nami.mixininterface.IEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static org.lwjgl.opengl.GL11C.GL_POLYGON_OFFSET_FILL;

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

        RotationsModule rotations = MODULE_MANAGER.getStorage().getByClass(RotationsModule.class);
        if (!rotations.render.get()) return;

        oldHeadYaw = MC.player.yHeadRot;
        oldPrevHeadYaw = MC.player.yHeadRotO;
        oldPitch = MC.player.getXRot();
        oldPrevPitch = MC.player.xRotO;

        float serverYaw = ROTATION_MANAGER.getStateHandler().getServerYaw();
        float serverPitch = ROTATION_MANAGER.getStateHandler().getServerPitch();

        MC.player.yHeadRot = serverYaw; //TODO interpolate it
        MC.player.yHeadRotO = serverYaw;
        MC.player.setXRot(serverPitch);
        MC.player.xRotO = serverPitch;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractRenderState1(T livingEntity, S state, float f, CallbackInfo ci) {
        if (!(livingEntity instanceof LocalPlayer player)) return;
        if (player != MC.player) return;

        RotationsModule rotations = MODULE_MANAGER.getStorage().getByClass(RotationsModule.class);
        if (!rotations.render.get()) return;

        MC.player.yHeadRot = oldHeadYaw;
        MC.player.yHeadRotO = oldPrevHeadYaw;
        MC.player.setXRot(oldPitch);
        MC.player.xRotO = oldPrevPitch;
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