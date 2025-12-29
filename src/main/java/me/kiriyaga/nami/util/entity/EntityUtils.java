package me.kiriyaga.nami.util.entity;

import me.kiriyaga.nami.mixin.DuckAnimal;
import me.kiriyaga.nami.mixin.DuckAgeableMob;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.kiriyaga.nami.Nami.*;

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
        if (MC.player == null || MC.level == null) return List.of();

        Vec3 playerPos = MC.player.position();
        List<Entity> all = getEntitiesBase(category);

        return all.stream()
                .filter(e -> e != MC.player)
                .filter(e -> e.distanceToSqr(playerPos) <= range * range)
                .filter(e -> extraFilter == null || extraFilter.test(e))
                .sorted(sortByDistance ? Comparator.comparingDouble(e -> e.distanceToSqr(playerPos)) : (a, b) -> 0)
                .collect(Collectors.toList());
    }

    private static List<Entity> getEntitiesBase(EntityTypeCategory category) {
        return switch (category) {
            case ALL -> getAllEntities();
            case PLAYERS -> getPlayers().stream().map(p -> (Entity) p).toList();
            case OTHER_PLAYERS -> getOtherPlayers().stream().map(p -> (Entity) p).toList();
            case HOSTILE -> getAllEntities().stream().filter(HostileUtils::isHostile).toList();
            case NEUTRAL -> getAllEntities().stream().filter(HostileUtils::isNeutral).toList();
            case PASSIVE -> getAllEntities().stream().filter(HostileUtils::isPassive).toList();
            case PROJECTILES -> getAllEntities().stream().filter(HostileUtils::isProjectile).toList();
            case DROPPED_ITEMS -> getAllEntities().stream().filter(e -> e instanceof ItemEntity).toList();
            case END_CRYSTALS -> getAllEntities().stream().filter(e -> e instanceof EndCrystal).toList();
        };
    }

    public static List<Entity> getAllEntities() {
        ClientLevel world = MC.level;
        return world != null
                ? StreamSupport.stream(world.entitiesForRendering().spliterator(), false).collect(Collectors.toList())
                : List.of();
    }

    public static List<Player> getPlayers() {
        ClientLevel world = MC.level;
        if (world == null) return List.of();

        return StreamSupport.stream(world.entitiesForRendering().spliterator(), false)
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .collect(Collectors.toList());
    }

    public static List<Player> getOtherPlayers() {
        LocalPlayer self = MC.player;
        return getPlayers().stream()
                .filter(p -> !p.isRemoved() && p != self)
                .collect(Collectors.toList());
    }

    public static boolean canBreed(Animal animal) {
        DuckAgeableMob a = (DuckAgeableMob) animal;
        DuckAnimal a1 = (DuckAnimal) animal;

        if (animal.isBaby()) return false;
        if (a.Age() != 0) return false;
        if (a1.InLove() > 0) return false;

        return animal.canFallInLove();
    }
}
