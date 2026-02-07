package namidevelopment.kiriyaga.api.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.RotationsFeatureConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    private float originalYaw;
    @Shadow
    private int noJumpDelay;
    @Shadow
    public float yHeadRot;
    private float originalPitch;

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void travelPreHook(Vec3 movementInput, CallbackInfo ci) {
        if (MC == null || MC.player != (Object)this) return;
        if (FEATURE_SERVICE.getStorage() == null) return;
        RotationsFeatureConfig config = FeatureContractService.get(RotationsFeatureConfig.class);
        if (config == null || !config.isMoveFixEnabled()) return;
        if (ROTATION_SERVICE == null || !ROTATION_SERVICE.getStateHandler().isRotating()) return;

        originalYaw = super.getYRot();
        originalPitch = super.getXRot();

        float spoofYaw = ROTATION_SERVICE.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_SERVICE.getStateHandler().getRotationPitch();

        this.setYRot(spoofYaw);
        this.setXRot(spoofPitch);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void travelPostHook(Vec3 movementInput, CallbackInfo ci) {
        if (MC == null || MC.player != (Object)this) return;
        if (ROTATION_SERVICE == null || !ROTATION_SERVICE.getStateHandler().isRotating()) return;

        if (FEATURE_SERVICE.getStorage() == null) return;
        RotationsFeatureConfig config = FeatureContractService.get(RotationsFeatureConfig.class);
        if (config == null || !config.isMoveFixEnabled()) return;

        this.setYRot(originalYaw);
        this.setXRot(originalPitch);
    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float jumpFix(float originalYaw) {
        if ((Object)this != MC.player) return originalYaw;
        return ROTATION_SERVICE.getStateHandler().isRotating() ? ROTATION_SERVICE.getStateHandler().getRotationYaw() : originalYaw;
    }
}
