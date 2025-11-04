package me.kiriyaga.nami.util;

import me.kiriyaga.nami.feature.module.impl.client.TargetModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.Nami.FRIEND_MANAGER;

public class EntityUtils {

    public enum EntityTypeCategory {
        ALL,
        PLAYERS,
        OTHER_PLAYERS,
        HOSTILE,
        NEUTRAL,
        PASSIVE,
        PROJECTILES,
        DROPPED_ITEMS,
        END_CRYSTALS
    }

    public static Entity getTarget() {
        TargetModule targetModule = MODULE_MANAGER.getStorage().getByClass(TargetModule.class);
        if (MC.player == null || MC.world == null || targetModule == null)
            return null;

        List<Entity> candidates = EntityUtils.getAllEntities().stream()
                .filter(e -> e != MC.player)
                .filter(e -> {
                    if (e instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) e;
                        if (!le.isAlive()) return false;
                        if (e.age < targetModule.minTicksExisted.get().intValue()) return false;
                        double distSq = e.squaredDistanceTo(MC.player);
                        if (distSq > targetModule.targetRange.get() * targetModule.targetRange.get()) return false;

                        return (targetModule.targetPlayers.get() && e instanceof PlayerEntity && !FRIEND_MANAGER.isFriend(e.getName().getString()))
                                || (targetModule.targetHostiles.get() && EntityUtils.isHostile(e))
                                || (targetModule.targetNeutrals.get() && EntityUtils.isNeutral(e))
                                || (targetModule.targetPassives.get() && EntityUtils.isPassive(e));
                    }

                    if (targetModule.targetPrijectiles.get()) {
                        return (e instanceof ShulkerBulletEntity) || (e instanceof FireballEntity);
                    }

                    return false;
                })
                .collect(Collectors.toList());

        switch (targetModule.priority.get()) {
            case HEALTH:
                return candidates.stream()
                        .filter(e -> e instanceof LivingEntity)
                        .min(Comparator.comparingDouble(e -> ((LivingEntity) e).getHealth()))
                        .orElse(null);

            case DISTANCE:
                return candidates.stream()
                        .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .orElse(null);

            case SMART:
                List<Entity> players = candidates.stream()
                        .filter(e -> e instanceof PlayerEntity && !FRIEND_MANAGER.isFriend(e.getName().getString()))
                        .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .toList();

                if (!players.isEmpty()) return players.get(0);

                List<Entity> creepers = candidates.stream()
                        .filter(e -> e instanceof CreeperEntity)
                        .filter(e -> e.squaredDistanceTo(MC.player) <= 3 * 3) // yeah its not accurate at all, but its not required here i guess?
                        .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .toList();

                if (!creepers.isEmpty()) return creepers.get(0);

                List<Entity> projectiles = candidates.stream()
                        .filter(e -> e instanceof ShulkerBulletEntity || e instanceof FireballEntity)
                        .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .toList();

                if (!projectiles.isEmpty()) return projectiles.get(0);

                List<Entity> others = candidates.stream()
                        .filter(e -> !(e instanceof PlayerEntity)
                                && !(e instanceof ShulkerBulletEntity)
                                && !(e instanceof FireballEntity))
                        .toList();

                return others.stream()
                        .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .orElse(null);
        }

        return null;
    }

    public static List<Entity> getEntities(EntityTypeCategory category) {
        return getEntities(category, Double.MAX_VALUE, false, null);
    }

    public static List<Entity> getEntities(EntityTypeCategory category, double range) {
        return getEntities(category, range, false, null);
    }

    public static List<Entity> getEntities(EntityTypeCategory category, double range, boolean sortByDistance) {
        return getEntities(category, range, sortByDistance, null);
    }

    public static List<Entity> getEntities(EntityTypeCategory category, double range, boolean sortByDistance, Predicate<Entity> extraFilter) {
        if (MC.player == null || MC.world == null) return List.of();

        Vec3d playerPos = MC.player.getPos();
        List<Entity> all = getEntitiesBase(category);

        return all.stream()
                .filter(e -> e != MC.player)
                .filter(e -> e.squaredDistanceTo(playerPos) <= range * range)
                .filter(e -> extraFilter == null || extraFilter.test(e))
                .sorted(sortByDistance ? Comparator.comparingDouble(e -> e.squaredDistanceTo(playerPos)) : (a, b) -> 0)
                .collect(Collectors.toList());
    }

    private static List<Entity> getEntitiesBase(EntityTypeCategory category) {
        return switch (category) {
            case ALL -> getAllEntities();
            case PLAYERS -> getPlayers().stream().map(p -> (Entity) p).toList();
            case OTHER_PLAYERS -> getOtherPlayers().stream().map(p -> (Entity) p).toList();
            case HOSTILE -> getAllEntities().stream().filter(EntityUtils::isHostile).toList();
            case NEUTRAL -> getAllEntities().stream().filter(EntityUtils::isNeutral).toList();
            case PASSIVE -> getAllEntities().stream().filter(EntityUtils::isPassive).toList();
            case PROJECTILES -> getAllEntities().stream().filter(EntityUtils::isProjectile).toList();
            case DROPPED_ITEMS -> getAllEntities().stream().filter(e -> e instanceof ItemEntity).toList();
            case END_CRYSTALS -> getAllEntities().stream().filter(e -> e instanceof EndCrystalEntity).toList();
        };
    }

    public static List<Entity> getAllEntities() {
        ClientWorld world = MC.world;
        return world != null
                ? StreamSupport.stream(world.getEntities().spliterator(), false).collect(Collectors.toList())
                : List.of();
    }

    public static List<PlayerEntity> getPlayers() {
        ClientWorld world = MC.world;
        if (world == null) return List.of();

        return StreamSupport.stream(world.getEntities().spliterator(), false)
                .filter(e -> e instanceof PlayerEntity)
                .map(e -> (PlayerEntity) e)
                .collect(Collectors.toList());
    }

    public static List<PlayerEntity> getOtherPlayers() {
        ClientPlayerEntity self = MC.player;
        return getPlayers().stream()
                .filter(p -> !p.isRemoved() && p != self)
                .collect(Collectors.toList());
    }

    public static boolean isProjectile(Entity e) {
        return e instanceof ShulkerBulletEntity
                || e instanceof FireballEntity
                || e instanceof WitherSkullEntity
                || e instanceof ArrowEntity
                || e instanceof TridentEntity
                || e instanceof ThrownEntity;
    }

    public static boolean isHostile(Entity e) {
        if (e instanceof CreeperEntity
                || e instanceof SkeletonEntity
                || e instanceof StrayEntity
                || e instanceof WitherSkeletonEntity
                || e instanceof ZombieEntity
                || e instanceof HuskEntity
                || e instanceof DrownedEntity
                || e instanceof VindicatorEntity
                || e instanceof BoggedEntity
                || e instanceof EvokerEntity
                || e instanceof PillagerEntity
                || e instanceof RavagerEntity
                || e instanceof BlazeEntity
                || e instanceof WitherEntity
                || e instanceof EnderDragonEntity
                || e instanceof ShulkerEntity
                || e instanceof GuardianEntity
                || e instanceof ElderGuardianEntity
                || e instanceof GhastEntity
                || e instanceof HoglinEntity
                || e instanceof ZombieVillagerEntity
                || e instanceof MagmaCubeEntity
                || e instanceof SilverfishEntity
                || e instanceof SlimeEntity
                || e instanceof PhantomEntity
                || e instanceof IllusionerEntity
                || e instanceof WitchEntity) {
            return true;
        }

        return isNeutralEntityType(e) && isAggressiveNow(e);
    }

    public static boolean isNeutral(Entity e) {
        return isNeutralEntityType(e) && !isAggressiveNow(e);
    }

    public static boolean isPassive(Entity e) {
        return e instanceof PassiveEntity ||
                (e instanceof IronGolemEntity golem && golem.isPlayerCreated());
    }

    private static boolean isNeutralEntityType(Entity e) {
        return e instanceof EndermanEntity ||
                e instanceof PiglinEntity ||
                e instanceof ZombifiedPiglinEntity ||
                e instanceof SpiderEntity ||
                e instanceof CaveSpiderEntity ||
                e instanceof PolarBearEntity ||
                (e instanceof WolfEntity w && !w.isTamed()) ||
                e instanceof BeeEntity ||
                e instanceof GoatEntity ||
                (e instanceof IronGolemEntity g && !g.isPlayerCreated());
    }

    public static boolean isAggressiveNow(Entity e) {
        ClientPlayerEntity player = MC.player;
        if (player == null || MC.world == null) return false;

        long timeOfDay = MC.world.getTimeOfDay() % 24000;
        boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000;

        if (e instanceof EndermanEntity enderman) return enderman.isAngry();
        if (e instanceof ZombifiedPiglinEntity piglin) return piglin.isAttacking();
        if (e instanceof PiglinEntity piglin) return !isPlayerWearingGold(player) || piglin.isAttacking();
        if (e instanceof SpiderEntity spider) return spider.isAttacking() || isNight;
        if (e instanceof CaveSpiderEntity) return true;
        if (e instanceof PolarBearEntity bear) return bear.isAttacking();
        if (e instanceof WolfEntity wolf) return wolf.isAttacking();
        if (e instanceof BeeEntity bee) return bee.hasAngerTime();

        return false;
    }

    private static boolean isPlayerWearingGold(ClientPlayerEntity player) {
        return Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmorSlot)
                .map(player::getEquippedStack)
                .anyMatch(EntityUtils::isGoldArmor);
    }

    private static boolean isGoldArmor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.GOLDEN_HELMET ||
                item == Items.GOLDEN_CHESTPLATE ||
                item == Items.GOLDEN_LEGGINGS ||
                item == Items.GOLDEN_BOOTS;
    }
}
