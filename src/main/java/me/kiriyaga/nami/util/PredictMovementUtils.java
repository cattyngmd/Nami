package me.kiriyaga.nami.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.IntFunction;

public final class PredictMovementUtils {

    private PredictMovementUtils() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("PredictMovementUtils");

    public record PredictedState(Vec3d pos, Vec3d velocity, float yaw, float pitch, boolean onGround, Vec3d eyePos) {}

    public static Optional<PredictedState> predict(Entity original, int ticks, IntFunction<Vec3d> inputProvider) {
        if (original == null || original.getWorld() == null) {
            return Optional.empty();
        }

        Entity e;
        if (original instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) original;
            e = new PlayerEntity(player.getWorld(), player.getX(), player.getY(), player.getZ());
            ((PlayerEntity) e).setHealth(player.getHealth());
        } else {
            e = original.getType().create(original.getWorld(), SpawnReason.CHUNK_GENERATION);
        }

        if (e == null) return Optional.empty();

        e.setPos(original.getX(), original.getY(), original.getZ());
        e.setVelocity(original.getVelocity());
        e.setYaw(original.getYaw());
        e.setPitch(original.getPitch());
        e.setHeadYaw(original.getYaw());
        try { e.setOnGround(original.isOnGround()); } catch (Throwable ignored) {}
        if (original instanceof LivingEntity origL && e instanceof LivingEntity cloneL) {
            cloneL.setHealth(origL.getHealth());
        }

        for (int t = 0; t < ticks; t++) {
            Vec3d input = inputProvider != null ? inputProvider.apply(t) : Vec3d.ZERO;
            try {
                if (e instanceof LivingEntity living) {
                    living.travel(input);
                }
            } catch (Throwable ex) {
                break;
            }
        }

        Vec3d pos = e.getPos();
        Vec3d vel = e.getVelocity();
        float yaw = e.getYaw();
        float pitch = e.getPitch();
        boolean onGround = e.isOnGround();
        Vec3d eyePos;
        try {
            eyePos = e.getEyePos();
        } catch (Throwable ex) {
            eyePos = pos.add(0, e.getStandingEyeHeight(), 0);
        }

        PredictedState result = new PredictedState(pos, vel, yaw, pitch, onGround, eyePos);
        return Optional.of(result);
    }

    public static Optional<PredictedState> predict(Entity original, int ticks, Vec3d constantInput) {
        return predict(original, ticks, i -> constantInput == null ? Vec3d.ZERO : constantInput);
    }

    public static Optional<PredictedState> predictOneTick(Entity original, Vec3d inputThisTick) {
        return predict(original, 1, i -> inputThisTick);
    }
}
