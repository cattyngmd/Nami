package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewModelModule;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    protected abstract boolean shouldInstantlyReplaceVisibleItem(ItemStack from, ItemStack to);

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 2), index = 0)
    private float modifyEquipProgressMainhand(float value) {
        ViewModelModule viewModelModule = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        boolean isOldAnimationsEnabled = viewModelModule != null && viewModelModule.isEnabled() && viewModelModule.oldAnimation.get();

        float attackCooldown = MC.player.getAttackStrengthScale(1f);
        float modifiedValue = isOldAnimationsEnabled ? 1f : attackCooldown * attackCooldown * attackCooldown;

        boolean skipAnimation = shouldInstantlyReplaceVisibleItem(mainHandItem, MC.player.getMainHandItem());

        return (skipAnimation ? modifiedValue : 0f) - mainHandHeight;
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void onRenderItem(AbstractClientPlayer abstractClientPlayerEntity, float f, float g, InteractionHand hand, float h, ItemStack itemStack, float i, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int j, CallbackInfo ci) {

        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        boolean isMainHand = hand == InteractionHand.MAIN_HAND;

        if (vm != null && vm.isEnabled() && !(isMainHand && itemStack.isEmpty() && !vm.hand.get())) {

            matrices.pushPose();

            float mirror = isMainHand ? 1.0f : -1.0f;

            matrices.mulPose(Axis.XP.rotationDegrees(vm.rotX.get().floatValue()));
            matrices.mulPose(Axis.YP.rotationDegrees(vm.rotY.get().floatValue() * mirror));
            matrices.mulPose(Axis.ZP.rotationDegrees(vm.rotZ.get().floatValue() * mirror));

            matrices.translate(
                    vm.posX.get().floatValue() * mirror,
                    vm.posY.get().floatValue(),
                    vm.posZ.get().floatValue()
            );

            float s = vm.scale.get().floatValue();
            matrices.scale(s, s, s);
        }
    }

//    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem" +
//                    "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;" +
//                    "Lnet/minecraft/item/ItemDisplayContext;" +
//                    "Lnet/minecraft/client/util/math/MatrixStack;" +
//                    "Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
//    private void scaleItems(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
//                            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
//                            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
//        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
//        if (vm != null && vm.isEnabled()) {
//            float s = vm.scale.get().floatValue();
//            matrices.scale(s, s, s);
//        }
//    }

    @Inject(method = "renderArmWithItem", at = @At("TAIL"))
    private void matricesPop(AbstractClientPlayer abstractClientPlayerEntity, float f, float g, InteractionHand hand, float h, ItemStack item, float i, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int j, CallbackInfo ci) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        boolean isMainHand = hand == InteractionHand.MAIN_HAND;

        if (vm != null && vm.isEnabled() && !(isMainHand && item.isEmpty() && !vm.hand.get())) {
            matrices.popPose();
        }
    }

    @Inject(method = "applyEatTransform", at = @At("HEAD"), cancellable = true)
    private void applyEatOrDrinkTransformation(PoseStack matrixStack, float tickDelta, HumanoidArm arm, ItemStack stack, Player player, CallbackInfo ci) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        if (vm != null && vm.isEnabled() && !vm.eating.get())
            ci.cancel();
    }

    @WrapOperation(
            method = "applyEatTransform(Lcom/mojang/blaze3d/vertex/PoseStack;FLnet/minecraft/world/entity/HumanoidArm;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void applyEatOrDrinkTransformation2(PoseStack matrices, float x, float y, float z, Operation<Void> original) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        if (vm != null && vm.isEnabled() && vm.eating.get()) {
            if (x == 0.0F && z == 0.0F) {
                double mul = vm.eatingBob.get();
                original.call(matrices, x, (float) (y * mul), z);
                return;
            }
        }
        original.call(matrices, x, y, z);
    }
}
