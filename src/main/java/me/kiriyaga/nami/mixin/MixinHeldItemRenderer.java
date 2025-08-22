package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.OldAnimationsModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewModelModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private ItemStack offHand;

    @Shadow
    protected abstract boolean shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to);

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 2), index = 0)
    private float modifyEquipProgressMainhand(float value) {
        OldAnimationsModule oldAnimations = MODULE_MANAGER.getStorage().getByClass(OldAnimationsModule.class);
        boolean isOldAnimationsEnabled = oldAnimations != null && oldAnimations.isEnabled();

        float attackCooldown = MC.player.getAttackCooldownProgress(1f);
        float modifiedValue = isOldAnimationsEnabled ? 1f : attackCooldown * attackCooldown * attackCooldown;

        boolean skipAnimation = shouldSkipHandAnimationOnSwap(mainHand, MC.player.getMainHandStack());

        return (skipAnimation ? modifiedValue : 0f) - equipProgressMainHand;
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", shift = At.Shift.BEFORE))
    private void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        if (vm != null && vm.isEnabled()) {
            boolean isOffhand = hand == Hand.OFF_HAND;
            float mirror = isOffhand ? -1.0f : 1.0f;

            matrices.translate(
                    vm.posX.get() * mirror,
                    vm.posY.get(),
                    vm.posZ.get()
            );

            float rx = vm.rotX.get().floatValue();
            float ry = vm.rotY.get().floatValue() * mirror;
            float rz = vm.rotZ.get().floatValue();

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rx));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ry));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rz));

            float s = vm.scale.get().floatValue();
            matrices.scale(s, s, s);
        }
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At("HEAD"), cancellable = true)
    private void applyEatOrDrinkTransformation(MatrixStack matrixStack, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        if (vm != null && vm.isEnabled() && !vm.eating.get())
            ci.cancel();
    }
}
