package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.kiriyaga.nami.feature.module.impl.exploits.ReachModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void pick(float tickDelta);

    @Shadow
    public abstract void resetData();

    @Shadow
    @Final
    private Camera mainCamera;

    @Unique
    private final PoseStack matrices = new PoseStack();

    @Shadow
    protected abstract void bobView(PoseStack matrices, float tickDelta);

    @Shadow
    protected abstract void bobHurt(PoseStack matrices, float tickDelta);


    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && floatingItem.getItem() == Items.TOTEM_OF_UNDYING && noRender.isEnabled() && noRender.noTotem.get()) {
            info.cancel();
        }
    }

    @Unique
    private boolean freecamSet = false;

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecamModule == null || !freecamModule.isEnabled()) return;

        if (minecraft == null) return;

        if (minecraft.getCameraEntity() != null && !freecamSet) {
            info.cancel();

            Entity cameraE = minecraft.getCameraEntity();

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

            if (minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null) {
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


/*    @ModifyReturnValue(method = "updateCrosshairTarget", at = @At("RETURN"))
    private HitResult findCrosshairTarget(HitResult original, HitResult hitResult) {
        ReachModule reachModule = MODULE_MANAGER.getStorage().getByClass(ReachModule.class);
        if (reachModule == null || !reachModule.isEnabled() || !reachModule.noEntityTrace.get()) {
            return original;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            boolean playerOnly = reachModule.playerOnly.get();
            boolean pickaxeOnly = reachModule.pickaxeOnly.get();

            var targetEntity = getTargetedEntity();
            var mainHandItem = MC.player.getMainHandStack().getItem();

            boolean lookingAtPlayer = targetEntity != null && targetEntity.isPlayer();
            boolean holdingPickaxe = isPickaxe(mainHandItem);

            if (playerOnly && pickaxeOnly) {
                if (lookingAtPlayer && holdingPickaxe) {
                    return hitResult;
                } else {
                    return original;
                }
            }

            if (playerOnly) {
                if (lookingAtPlayer) {
                    return hitResult;
                } else {
                    return original;
                }
            }

            if (pickaxeOnly) {
                if (holdingPickaxe) {
                    return hitResult;
                } else {
                    return original;
                }
            }

            return hitResult;
        }

        return original;
    }*/

    private Entity getTargetedEntity() {
        if (MC.hitResult != null && MC.hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) MC.hitResult).getEntity();
        }
        return null;
    }

    private boolean isPickaxe(Item item) {
        return item.getDefaultInstance().is(ItemTags.PICKAXES);
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