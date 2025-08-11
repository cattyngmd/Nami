package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.*;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;

import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.*;

import me.kiriyaga.nami.setting.impl.*;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.*;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class VelocityModule extends Module {

    private enum Mode {
        VANILLA,
        WALLS
    }

    private final EnumSetting<Mode> modeSetting = addSetting(new EnumSetting<>("mode", Mode.WALLS));
    private final DoubleSetting horizontalPercent = addSetting(new DoubleSetting("horizontal", 100.0, 0.0, 100.0));
    private final DoubleSetting verticalPercent = addSetting(new DoubleSetting("vertical", 100.0, 0.0, 100.0));
    private final BoolSetting handleKnockback = addSetting(new BoolSetting("knockback", true));
    private final BoolSetting handleExplosions = addSetting(new BoolSetting("explosion", true));
    private final BoolSetting concealMotion = addSetting(new BoolSetting("conceal", false));
    private final BoolSetting requireGround = addSetting(new BoolSetting("ground only", false));
    private final BoolSetting cancelEntityPush = addSetting(new BoolSetting("entity push", true));
    private final BoolSetting cancelBlockPush = addSetting(new BoolSetting("block push", true));
    private final BoolSetting cancelLiquidPush = addSetting(new BoolSetting("liquid push", true));
    private final BoolSetting cancelFishHook = addSetting(new BoolSetting("rod push", false));

    private boolean pendingConcealment = false;

    public VelocityModule() {
        super("velocity", "Reduces or modifies incoming velocity effects.", ModuleCategory.of("movement"),
                "antiknockback", "мудщсшен");
    }

    @Override
    public void onDisable() {
        pendingConcealment = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPacketReceive(PacketReceiveEvent event) {
        ClientPlayerEntity player = MC.player;
        if (player == null || MC.world == null) return;

        Packet<?> packet = event.getPacket();

        if (packet instanceof PlayerPositionLookS2CPacket && concealMotion.get()) {
            pendingConcealment = true;
        }

        if (packet instanceof EntityVelocityUpdateS2CPacket velocityPacket && handleKnockback.get()) {
            if (velocityPacket.getEntityId() != player.getId()) return;

            if (pendingConcealment && isZeroVelocity(velocityPacket)) {
                pendingConcealment = false;
                return;
            }

            if (modeSetting.get() == Mode.WALLS && (!isPlayerPhased() || (requireGround.get() && !player.isOnGround()))) {
                return;
            }

            switch (modeSetting.get()) {
                case VANILLA, WALLS -> {
                    if (isNoVelocityConfigured()) {
                        event.cancel();
                    } else {
                        scaleVelocityPacket(velocityPacket);
                    }
                }
            }
        }

        else if (packet instanceof ExplosionS2CPacket explosionPacket && handleExplosions.get()) {
            boolean phased = isPlayerPhased();

            switch (modeSetting.get()) {
                case VANILLA -> {
                    if (isNoVelocityConfigured()) {
                        event.cancel();
                    } else {
                        scaleExplosionPacket(explosionPacket);
                    }
                }
                case WALLS -> {
                    if (!phased) return;

                    if (isNoVelocityConfigured()) {
                        event.cancel();
                    } else {
                        scaleExplosionPacket(explosionPacket);
                    }
                }
            }
        }


        else if (packet instanceof BundleS2CPacket bundlePacket) {
            handleBundlePacket(event, bundlePacket);
        }

        else if (packet instanceof EntityStatusS2CPacket statusPacket
                && statusPacket.getStatus() == EntityStatuses.PULL_HOOKED_ENTITY
                && cancelFishHook.get()) {
            Entity entity = statusPacket.getEntity(MC.world);
            if (entity instanceof FishingBobberEntity hook && hook.getHookedEntity() == player) {
                event.cancel();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPreTick(PreTickEvent event) {
        pendingConcealment = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEntityPush(EntityPushEvent event) {
        if (cancelEntityPush.get() && event.getTarget().equals(MC.player)) {
            event.cancel();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onBlockPush(BlockPushEvent event) {
        if (cancelBlockPush.get()) {
            event.cancel();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onFluidPush(LiquidPushEvent event) {
        if (cancelLiquidPush.get()) {
            event.cancel();
        }
    }

    private void handleBundlePacket(PacketReceiveEvent event, BundleS2CPacket bundle) {
        List<Packet<?>> filteredPackets = new ArrayList<>();

        for (Packet<?> p : bundle.getPackets()) {
            if (p instanceof ExplosionS2CPacket explosion && handleExplosions.get()) {
                boolean phased = isPlayerPhased();

                switch (modeSetting.get()) {
                    case VANILLA -> {
                        if (!isNoVelocityConfigured()) {
                            scaleExplosionPacket(explosion);
                        } else continue;
                    }
                    case WALLS -> {
                        if (!phased) {
                            filteredPackets.add(p);
                            continue;
                        }
                        if (!isNoVelocityConfigured()) {
                            scaleExplosionPacket(explosion);
                        } else continue;
                    }
                }

                filteredPackets.add(p);
            }


            else if (p instanceof EntityVelocityUpdateS2CPacket velocity && handleKnockback.get()) {
                if (velocity.getEntityId() != MC.player.getId()) {
                    filteredPackets.add(p);
                    continue;
                }
                
                if (modeSetting.get() == Mode.WALLS) {
                    if (!isPlayerPhased() || (requireGround.get() && !MC.player.isOnGround())) {
                        filteredPackets.add(p);
                        continue;
                    }
                }

                if (isNoVelocityConfigured()) continue;
                scaleVelocityPacket(velocity);
            }

            filteredPackets.add(p);
        }

        ((BundlePacketAccessor) bundle).setIterable(filteredPackets);
    }

    private boolean isZeroVelocity(EntityVelocityUpdateS2CPacket packet) {
        return packet.getVelocityX() == 0 && packet.getVelocityY() == 0 && packet.getVelocityZ() == 0;
    }

    private boolean isNoVelocityConfigured() {
        return horizontalPercent.get() == 0 && verticalPercent.get() == 0;
    }

    private boolean isPlayerPhased() {
        ClientPlayerEntity player = MC.player;
        if (player == null || MC.world == null) return false;

        Box boundingBox = player.getBoundingBox();

        int minX = MathHelper.floor(boundingBox.minX);
        int maxX = MathHelper.ceil(boundingBox.maxX);
        int minY = MathHelper.floor(boundingBox.minY);
        int maxY = MathHelper.ceil(boundingBox.maxY);
        int minZ = MathHelper.floor(boundingBox.minZ);
        int maxZ = MathHelper.ceil(boundingBox.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = MC.world.getBlockState(pos).getCollisionShape(MC.world, pos);

                    if (!shape.isEmpty() && shape.getBoundingBox().offset(pos).intersects(boundingBox)) {
                        //CHAT_MANAGER.sendRaw("phased");
                        return true;
                    }
                }
            }
        }

        return false;
    }


    private void scaleVelocityPacket(EntityVelocityUpdateS2CPacket packet) {
        int scaledX = (int) (packet.getVelocityX() * (horizontalPercent.get() / 100.0));
        int scaledY = (int) (packet.getVelocityY() * (verticalPercent.get() / 100.0));
        int scaledZ = (int) (packet.getVelocityZ() * (horizontalPercent.get() / 100.0));

        ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityX(scaledX);
        ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityY(scaledY);
        ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityZ(scaledZ);
    }

    private void scaleExplosionPacket(ExplosionS2CPacket packet) {
        ExplosionS2CPacketAccessor accessor = (ExplosionS2CPacketAccessor) (Object) packet;

        accessor.getPlayerKnockback().ifPresent(original -> {
            Vec3d scaled = new Vec3d(
                    original.x * (horizontalPercent.get() / 100.0),
                    original.y * (verticalPercent.get() / 100.0),
                    original.z * (horizontalPercent.get() / 100.0)
            );
            accessor.setPlayerKnockback(Optional.of(scaled));
        });
    }
}