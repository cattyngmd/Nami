package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.impl.feature.impl.exploits.ReachFeature;
import me.kiriyaga.nami.impl.feature.impl.visuals.FreecamFeature;
import me.kiriyaga.nami.impl.feature.impl.visuals.NoRenderFeature;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.FEATURE_SERVICE;
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
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(" +
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
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void captureMatrices(
            DeltaTracker deltaTracker,
            CallbackInfo ci,
            float f,
            LocalPlayer localPlayer,
            ProfilerFiller profilerFiller,
            boolean bl,
            float g,
            Matrix4f projection,      // matrix4f
            PoseStack poseStack,
            float h,
            float i,
            float j,
            float k,
            Quaternionf quaternionf,
            Matrix4f view             // matrix4f2
    ) {
        PROJECTION_MATRIX.set(projection);
        MODEL_VIEW_MATRIX.set(view);
    }



    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void displayItemActivation(ItemStack floatingItem, CallbackInfo info) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        NoRenderFeature noRender = FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class);
        if (noRender != null && floatingItem.getItem() == Items.TOTEM_OF_UNDYING && noRender.isEnabled() && noRender.noTotem.get()) {
            info.cancel();
        }
    }

    @Unique
    private boolean freecamSet = false;

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void pick1(float tickDelta, CallbackInfo info) {
        if (FEATURE_SERVICE.getStorage() == null) return;

        FreecamFeature freecamFeature = FEATURE_SERVICE.getStorage().getByClass(FreecamFeature.class);
        if (freecamFeature == null || !freecamFeature.isEnabled()) return;

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

            cameraE.setPosRaw(freecamFeature.getX(), freecamFeature.getY() - cameraE.getEyeHeight(cameraE.getPose()), freecamFeature.getZ());

            cameraE.xo = freecamFeature.prevPos.x;
            cameraE.yo = freecamFeature.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.zo = freecamFeature.prevPos.z;

            cameraE.setYRot(freecamFeature.yaw);
            cameraE.setXRot(freecamFeature.pitch);
            cameraE.yRotO = freecamFeature.lastYaw;
            cameraE.xRotO = freecamFeature.lastPitch;

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

        ReachFeature reach = FEATURE_SERVICE.getStorage().getByClass(ReachFeature.class);
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
        if (FEATURE_SERVICE.getStorage() != null && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class) != null && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class).isEnabled() && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class).noBob.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        if (FEATURE_SERVICE.getStorage() != null && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class) != null && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class).isEnabled() && FEATURE_SERVICE.getStorage().getByClass(NoRenderFeature.class).noTilt.get()) {
            ci.cancel();
        }
    }
}