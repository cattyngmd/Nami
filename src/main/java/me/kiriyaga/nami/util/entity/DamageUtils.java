package me.kiriyaga.nami.util.entity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.kiriyaga.nami.util.EnchantmentUtils;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import java.util.function.BiFunction;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.util.RotationUtils.getClosestPointToEye;

public class DamageUtils {

    public static final BlockRaycastProvider BLOCK_CHECK = (ctx, pos) -> {
        BlockState state = MC.world.getBlockState(pos);
        if (state.getBlock().getBlastResistance() < 600) return null;
        return state.getCollisionShape(MC.world, pos).raycast(ctx.start(), ctx.end(), pos);
    };

    public static float crystalDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, BlockRaycastProvider raycastProvider) {
        return computeExplosionDamage(target, targetPos, targetBox, explosionPos, 12f, raycastProvider);
    }

    public static float bedDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, BlockRaycastProvider raycastProvider) {
        return computeExplosionDamage(target, targetPos, targetBox, explosionPos, 10f, raycastProvider);
    }

    public static float anchorDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, BlockRaycastProvider raycastProvider) {
        return computeExplosionDamage(target, targetPos, targetBox, explosionPos, 10f, raycastProvider);
    }

    private static float computeExplosionDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, float strength, BlockRaycastProvider raycastProvider) {
        Vec3d lookDir = getClosestPointToEye(explosionPos, target.getBoundingBox()).subtract(explosionPos).normalize();
        Vec3d rayEnd = explosionPos.add(lookDir.multiply(strength));

        if (target.getBoundingBox().raycast(explosionPos, rayEnd).isEmpty()) return 0f;

        double distance = targetPos.distanceTo(explosionPos);
        double exposure = calculateExposure(explosionPos, targetBox, raycastProvider);
        double impact = (1 - (distance / strength)) * exposure;
        float baseDamage = (float) ((impact * impact + impact) / 2 * 7 * 12 + 1);

        return applyReductions(baseDamage, target, MC.world.getDamageSources().explosion(null));
    }

    public static float applyReductions(float damage, Entity entity, DamageSource source) {
        if (source.isScaledWithDifficulty()) {
            switch (MC.world.getDifficulty()) {
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }

        if (!(entity instanceof LivingEntity living)) return Math.max(damage, 0);

        damage = DamageUtil.getDamageLeft(living, damage, source, (float) Math.floor(living.getAttributeValue(EntityAttributes.ARMOR)),
                (float) living.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
        damage = reduceByResistance(living, damage);
        damage = reduceByProtection(living, damage, source);

        return Math.max(damage, 0);
    }

    private static float reduceByProtection(LivingEntity entity, float damage, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return damage;

        int totalProtection = 0;

        for (EquipmentSlot slot : AttributeModifierSlot.ARMOR) {
            ItemStack stack = entity.getEquippedStack(slot);
            int prot = EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.PROTECTION);
            if (prot > 0) totalProtection += prot;

            if (source.isIn(DamageTypeTags.IS_FIRE)) totalProtection += 2 * EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.FIRE_PROTECTION);
            if (source.isIn(DamageTypeTags.IS_EXPLOSION)) totalProtection += 2 * EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.BLAST_PROTECTION);
            if (source.isIn(DamageTypeTags.IS_PROJECTILE)) totalProtection += 2 * EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.PROJECTILE_PROTECTION);
            if (source.isIn(DamageTypeTags.IS_FALL)) totalProtection += 3 * EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.FEATHER_FALLING);
        }

        return DamageUtil.getInflictedDamage(damage, totalProtection);
    }

    private static float reduceByResistance(LivingEntity entity, float damage) {
        StatusEffectInstance resistance = entity.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) damage *= 1 - 0.2f * (resistance.getAmplifier() + 1);
        return Math.max(damage, 0);
    }

    private static float calculateExposure(Vec3d source, Box box, BlockRaycastProvider provider) {
        double dx = box.getLengthX();
        double dy = box.getLengthY();
        double dz = box.getLengthZ();

        int steps = 2;
        int hits = 0, misses = 0;

        for (double x = 0; x <= dx; x += dx / steps) {
            for (double y = 0; y <= dy; y += dy / steps) {
                for (double z = 0; z <= dz; z += dz / steps) {
                    Vec3d pos = new Vec3d(box.minX + x, box.minY + y, box.minZ + z);
                    if (raycast(new ExposureContext(pos, source), provider) == null) misses++;
                    hits++;
                }
            }
        }

        return hits == 0 ? 0f : (float) misses / hits;
    }

    private static BlockHitResult raycast(ExposureContext context, BlockRaycastProvider provider) {
        return BlockView.raycast(context.start, context.end, context, provider, ctx -> null);
    }

    public record ExposureContext(Vec3d start, Vec3d end) {}

    @FunctionalInterface
    public interface BlockRaycastProvider extends BiFunction<ExposureContext, net.minecraft.util.math.BlockPos, BlockHitResult> {}
}
