package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.kiriyaga.nami.feature.module.impl.exploits.ReachModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.util.render.RenderUtil.MODEL_VIEW_MATRIX;
import static me.kiriyaga.nami.util.render.RenderUtil.PROJECTION_MATRIX;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    public abstract void pick(float tickDelta);

    @Inject(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(" +
                                    "Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;" +
                                    "Lnet/minecraft/client/DeltaTracker;" +
                                    "Z" +
                                    "Lnet/minecraft/client/Camera;" +
                                    "Lorg/joml/Matrix4f;" +
                                    "Lorg/joml/Matrix4f;" +
                                    "Lorg/joml/Matrix4f;" +
                                    "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;" +
                                    "Lorg/joml/Vector4f;" +
                                    "Z)V"
            )
    )
    private void captureMatrices(
            DeltaTracker deltaTracker,
            CallbackInfo ci,
            @Local(name = "matrix4f") Matrix4f projection,
            @Local(name = "matrix4f2") Matrix4f view
    ) {
        PROJECTION_MATRIX.set(projection);
        MODEL_VIEW_MATRIX.set(view);
    }


    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void displayItemActivation(ItemStack floatingItem, CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && floatingItem.getItem() == Items.TOTEM_OF_UNDYING && noRender.isEnabled() && noRender.noTotem.get()) {
            info.cancel();
        }
    }

    @Unique
    private boolean freecamSet = false;

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void pick1(float tickDelta, CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecamModule == null || !freecamModule.isEnabled()) return;

        if (MC == null) return;

        if (MC.getCameraEntity() != null && !freecamSet) {
            info.cancel();

            Entity cameraE = MC.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double lastX = cameraE.xo;
            double lastY = cameraE.yo;
            double lastZ = cameraE.zo;
            float yaw = cameraE.getYRot();
            float pitch = cameraE.getXRot();
            float lastYaw = cameraE.yRotO;
            float lastPitch = cameraE.xRotO;

            cameraE.setPosRaw(freecamModule.getX(), freecamModule.getY() - cameraE.getEyeHeight(cameraE.getPose()), freecamModule.getZ());

            cameraE.xo = freecamModule.prevPos.x;
            cameraE.yo = freecamModule.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.zo = freecamModule.prevPos.z;

            cameraE.setYRot(freecamModule.yaw);
            cameraE.setXRot(freecamModule.pitch);
            cameraE.yRotO = freecamModule.lastYaw;
            cameraE.xRotO = freecamModule.lastPitch;

            freecamSet = true;

            if (MC.gameRenderer != null && MC.gameRenderer.getMainCamera() != null) {
                pick(tickDelta);
            }

            freecamSet = false;

            cameraE.setPosRaw(x, y, z);
            cameraE.xo = lastX;
            cameraE.yo = lastY;
            cameraE.zo = lastZ;
            cameraE.setYRot(yaw);
            cameraE.setXRot(pitch);
            cameraE.yRotO = lastYaw;
            cameraE.xRotO = lastPitch;
        }
    }


    @Inject(method = "pick", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;"))
    private void pick(float f, CallbackInfo ci) {
        HitResult hit = MC.player.raycastHitResult(f, MC.getCameraEntity());

        HitResult modified = modifyHit(hit);

        MC.hitResult = modified;
    }

    private HitResult modifyHit(HitResult hit) {

        ReachModule reach = MODULE_MANAGER.getStorage().getByClass(ReachModule.class);
        if (reach == null || !reach.isEnabled() || !reach.noEntityTrace.get())
            return hit;

        if (hit.getType() == HitResult.Type.BLOCK) {

            Entity targetEntity = getTargetedEntity();
            Item mainHandItem = MC.player.getMainHandItem().getItem();

            boolean lookingAtPlayer = targetEntity instanceof Player;
            boolean holdingPickaxe = mainHandItem.getDefaultInstance().is(ItemTags.PICKAXES);

            if (reach.playerOnly.get() && !lookingAtPlayer)
                return hit;

            if (reach.pickaxeOnly.get() && !holdingPickaxe)
                return hit;
        }

        return hit;
    }

    private Entity getTargetedEntity() {
        if (MC.hitResult != null && MC.hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) MC.hitResult).getEntity();
        }
        return null;
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class) != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).noBob.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class) != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).noTilt.get()) {
            ci.cancel();
        }
    }
}