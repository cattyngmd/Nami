package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.HeldItemRendererEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.OldAnimationsModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        HeldItemRendererEvent ev = new HeldItemRendererEvent(hand, matrices);

        EVENT_MANAGER.post(ev);
    }

}
