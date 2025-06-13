package me.kiriyaga.essentials.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ItemEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.kiriyaga.essentials.Essentials.*;

public class EntityUtils {

    public static List<Entity> getAllEntities() {
        ClientWorld world = MINECRAFT.world;
        return world != null
                ? StreamSupport.stream(world.getEntities().spliterator(), false)
                .collect(Collectors.toList())
                : List.of();
    }

    public static List<PlayerEntity> getPlayers() {
        ClientWorld world = MINECRAFT.world;
        return world != null ? world.getPlayers().stream().collect(Collectors.toList()) : List.of();
    }

    public static List<Entity> getMobs() {
        return getAllEntities().stream()
                .filter(e -> e instanceof MobEntity)
                .collect(Collectors.toList());
    }

    public static List<Entity> getPassiveMobs() {
        return getAllEntities().stream()
                .filter(e -> e instanceof PassiveEntity
                        || (e instanceof IronGolemEntity && !((IronGolemEntity) e).isPlayerCreated()))
                .collect(Collectors.toList());
    }

    public static List<Entity> getNeutralMobs() {
        return getAllEntities().stream()
                .filter(e -> isNeutralMob(e))
                .collect(Collectors.toList());
    }

    public static List<Entity> getHostileMobs() {
        return getAllEntities().stream()
                .filter(e -> e instanceof HostileEntity && !isNeutralMob(e))
                .collect(Collectors.toList());
    }

    public static List<ItemEntity> getDroppedItems() {
        return getAllEntities().stream()
                .filter(e -> e instanceof ItemEntity)
                .map(e -> (ItemEntity) e)
                .collect(Collectors.toList());
    }

    public static List<Entity> getEndCrystals() {
        return getAllEntities().stream()
                .filter(e -> e instanceof EndCrystalEntity)
                .collect(Collectors.toList());
    }

    public static List<Entity> getOtherPlayers() {
        ClientPlayerEntity self = MINECRAFT.player;
        return getPlayers().stream()
                .filter(p -> !p.isRemoved() && p != self)
                .collect(Collectors.toList());
    }

    private static boolean isNeutralMob(Entity e) {
        return e instanceof BeeEntity
                || e instanceof CaveSpiderEntity
                || e instanceof DolphinEntity
                || e instanceof DrownedEntity
                || e instanceof EndermanEntity
                || e instanceof FoxEntity
                || e instanceof GoatEntity
                || (e instanceof IronGolemEntity && !((IronGolemEntity)e).isPlayerCreated())
                || e instanceof LlamaEntity
                || e instanceof TraderLlamaEntity
                || e instanceof PandaEntity
                || e instanceof PiglinEntity
                || e instanceof PolarBearEntity
                || e instanceof SpiderEntity
                || (e instanceof WolfEntity && !((WolfEntity)e).isTamed())
                || e instanceof ZombifiedPiglinEntity;
    }
}
