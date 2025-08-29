package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.MoveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.core.rotation.*;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ElytraFlyModule extends Module {

    public enum FlyMode {
        BOUNCE, CONTROL, GLIDE
    }

    public final EnumSetting<FlyMode> mode = addSetting(new EnumSetting<>("mode", FlyMode.BOUNCE));

    // GLIDE
    private final IntSetting targetY = addSetting(new IntSetting("targetY", 180, 60, 600));
    private final IntSetting vLow = addSetting(new IntSetting("min speed", 14, 6, 40));
    private final IntSetting vHigh = addSetting(new IntSetting("max speed", 27, 10, 60));
    private final IntSetting climbPitch = addSetting(new IntSetting("climb pitch", 40, 0, 60));
    private final IntSetting divePitch = addSetting(new IntSetting("dive pitch", 38, 20, 60));
    private final IntSetting cruiseMin = addSetting(new IntSetting("cruise min", 4, 0, 20));
    private final IntSetting cruiseMax = addSetting(new IntSetting("cruise max", 12, 2, 25));
    private final BoolSetting allowRockets = addSetting(new BoolSetting("allow rockets", true));
    private final IntSetting rocketSpeed = addSetting(new IntSetting("rocket below", 9, 0, 30));

    //
    // CONTROL
    //private final BoolSetting midAirFreeze = addSetting(new BoolSetting("mid air freeze", false));
    private final BoolSetting lockPitch = addSetting(new BoolSetting("lock pitch", true));

    // BOOST
    private final BoolSetting boost = addSetting(new BoolSetting("boost", false));
    private final BoolSetting newBoost = addSetting(new BoolSetting("new boost", false));
    private final BoolSetting pitch = addSetting(new BoolSetting("pitch", true));
    private final IntSetting pitchDegree = addSetting(new IntSetting("pitch", 75, 0, 90));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    private enum GlideState { DIVE, CRUISE, CLIMB }
    private GlideState glideState = GlideState.CRUISE;
    private double speed = 0;
    private double[] speedSamples = new double[25];
    private int speedSampleIndex = 0;
    private boolean speedBufferFilled = false;
    private double baseY = 0;
    private double lastX = 0;
    private double lastZ = 0;
    private double cruisePhase = 0;
    private long rocket = 0;
    private boolean climbingToTarget = false;

    public ElytraFlyModule() {
        super("elytra fly", "Improves elytra flying.", ModuleCategory.of("movement"), "elytrafly");
        boost.setShowCondition(() -> mode.get() == FlyMode.BOUNCE);
        newBoost.setShowCondition(() -> mode.get() == FlyMode.BOUNCE);
        pitch.setShowCondition(() -> mode.get() == FlyMode.BOUNCE);
        pitchDegree.setShowCondition(() -> mode.get() == FlyMode.BOUNCE && pitch.get());
        lockPitch.setShowCondition(() -> mode.get() == FlyMode.CONTROL);
        vLow.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        vHigh.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        climbPitch.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        divePitch.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        cruiseMin.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        cruiseMax.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        allowRockets.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
        rocketSpeed.setShowCondition(() -> mode.get() == FlyMode.GLIDE && allowRockets.get());
        targetY.setShowCondition(() -> mode.get() == FlyMode.GLIDE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (MC.player != null) {
            baseY = MC.player.getY();
            glideState = GlideState.CLIMB;
            cruisePhase = 0;
        }
    }

    @Override
    public void onDisable() {
        if (mode.get() == FlyMode.BOUNCE) setJumpHeld(false);
        baseY = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onMove(MoveEvent event) {
        ClientPlayerEntity player = MC.player;
        if (player == null || mode.get() != FlyMode.BOUNCE) return;

        if (!player.isOnGround()) return;

        if (player.isSprinting() && speed > 12.00 && newBoost.get()) {

            Vec3d velocity = player.getVelocity();
            event.setMovement(new Vec3d(velocity.x, 0, velocity.z));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onPreTick(PreTickEvent event) {
        if (MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        this.setDisplayInfo(mode.get().toString());

        if (mode.get() == FlyMode.BOUNCE) {
            setJumpHeld(true);

            if (boost.get()) {
                MC.player.setVelocity(MC.player.getVelocity().x, 0.0, MC.player.getVelocity().z);
            }

            if (pitch.get())
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(this.getName(), rotationPriority.get(), MC.player.getYaw(), pitchDegree.get().floatValue()));

            MC.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
            );
        } else
        if (mode.get() == FlyMode.CONTROL) {
            if (!MC.player.isGliding()) return;

            Vec3d dir = getControlDirection();

//            if (midAirFreeze.get() && dir == null) {
//                float yaw = MC.player.getYaw();
//                float pitchFreeze = -3f;
//
//                double forwardMotion = (MC.player.age % 8 < 4) ? 0.1 : -0.1;
//
//                double radYaw = Math.toRadians(yaw);
//                Vec3d freezeVel = new Vec3d(
//                        -Math.sin(radYaw) * forwardMotion,
//                        0,
//                        Math.cos(radYaw) * forwardMotion
//                );
//
//                MC.player.setVelocity(freezeVel);
//
//                ROTATION_MANAGER.getRequestHandler().submit(
//                        new RotationRequest(this.getName(), rotationPriority.get(), yaw, pitchFreeze)
//                );
//
//                setJumpHeld(true);
//                return;
//            }

            if (dir != null) {
                float finalYaw;
                float finalPitch;

                if (Math.abs(dir.y) > 0.5) {
                    finalYaw = MC.player.getYaw();
                    finalPitch = dir.y > 0 ? -90f : 90f;
                } else {
                    finalYaw = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90f;
                    finalPitch = MC.player.getPitch();
                    if (lockPitch.get()) {
                        finalPitch = -3f;
                    }
                }

                ROTATION_MANAGER.getRequestHandler().submit(
                        new RotationRequest(this.getName(), rotationPriority.get(), finalYaw, finalPitch)
                );

                setJumpHeld(true);
            }
        } else if (mode.get() == FlyMode.GLIDE) {
            if (!MC.player.isGliding())
                return;

            double playerY = MC.player.getY();
            double target = targetY.get();
            long currentTime = System.currentTimeMillis();

            if (!climbingToTarget && playerY < target - 80) {
                climbingToTarget = true;
            }

            GlideState effectiveState;

            if (climbingToTarget) {
                effectiveState = GlideState.CLIMB;

                if (allowRockets.get() && currentTime - rocket >= 3500) {
                    useItemAnywhere(Items.FIREWORK_ROCKET);
                    rocket = currentTime;
                }

                if (playerY >= target) {
                    climbingToTarget = false;
                }
            } else {
                final double v = speed;
                final int vLowVal = vLow.get();
                final int vHighVal = Math.max(vHigh.get(), vLowVal + 2);

                switch (glideState) {
                    case DIVE:
                        if (v >= vHighVal) glideState = GlideState.CRUISE;
                        break;
                    case CLIMB:
                        if (v <= vLowVal) glideState = GlideState.DIVE;
                        break;
                    case CRUISE:
                        if (v <= vLowVal - 1) glideState = GlideState.DIVE;
                        else if (v >= vHighVal + 2) glideState = GlideState.CLIMB;
                        break;
                }
                effectiveState = glideState;
            }

            float targetPitch;
            if (effectiveState == GlideState.DIVE) {
                targetPitch = clampPitch(+divePitch.get());
            } else if (effectiveState == GlideState.CLIMB) {
                targetPitch = clampPitch(-climbPitch.get());
            } else {
                targetPitch = cruisePitch();
            }

            float currentPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();
            float smoothPitch = approach(currentPitch, targetPitch, 10);

            //TODO yaw smooth n
            ROTATION_MANAGER.getRequestHandler().submit(
                    new RotationRequest(this.getName(), rotationPriority.get(), MC.player.getYaw(), smoothPitch)
            );
        }
    }


    private float cruisePitch() {
        double dt = 1.0 / 20.0;
        double period = 2.6;
        cruisePhase += dt / period;
        if (cruisePhase > 1.0) cruisePhase -= 1.0;
        double tri = 2.0 * Math.abs(2.0 * (cruisePhase - Math.floor(cruisePhase + 0.5))) - 1.0;
        tri = Math.copySign(tri * tri, tri);
        double min = cruiseMin.get();
        double max = Math.max(cruiseMax.get(), cruiseMin.get() + 1);
        double ampPitch = min + (max - min) * (0.5 * (tri + 1.0));
        return clampPitch((float) -ampPitch);
    }


    private float approach(float current, float target, float maxDelta) {
        float delta = MathHelper.clamp(target - current, -maxDelta, +maxDelta);
        return current + delta;
    }


    private float clampPitch(float pitchDeg) {
        return MathHelper.clamp(pitchDeg, -89f, 89f);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onPreTick2(PreTickEvent event) {
        if (MC.player == null) return;

        double dx = MC.player.getX() - lastX;
        double dz = MC.player.getZ() - lastZ;

        double instantSpeed = Math.sqrt(dx * dx + dz * dz) * 20;

        speedSamples[speedSampleIndex] = instantSpeed;
        speedSampleIndex = (speedSampleIndex + 1) % speedSamples.length;

        if (speedSampleIndex == 0) speedBufferFilled = true;

        int count = speedBufferFilled ? speedSamples.length : speedSampleIndex;
        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += speedSamples[i];
        }

        speed = count > 0 ? sum / count : 0;

        lastX = MC.player.getX();
        lastZ = MC.player.getZ();
    }

    private void setJumpHeld(boolean held) {
        KeyBinding jumpKey = MC.options.jumpKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) jumpKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        jumpKey.setPressed(physicallyPressed || held);
    }

    private Vec3d getControlDirection() {
        boolean forward = InputUtil.isKeyPressed(MC.getWindow().getHandle(), ((KeyBindingAccessor) MC.options.forwardKey).getBoundKey().getCode());
        boolean back    = InputUtil.isKeyPressed(MC.getWindow().getHandle(), ((KeyBindingAccessor) MC.options.backKey).getBoundKey().getCode());
        boolean left    = InputUtil.isKeyPressed(MC.getWindow().getHandle(), ((KeyBindingAccessor) MC.options.leftKey).getBoundKey().getCode());
        boolean right   = InputUtil.isKeyPressed(MC.getWindow().getHandle(), ((KeyBindingAccessor) MC.options.rightKey).getBoundKey().getCode());
        boolean up      = InputUtil.isKeyPressed(MC.getWindow().getHandle(), ((KeyBindingAccessor) MC.options.jumpKey).getBoundKey().getCode());
        boolean down    = InputUtil.isKeyPressed(MC.getWindow().getHandle(), ((KeyBindingAccessor) MC.options.sneakKey).getBoundKey().getCode());

        if (!(forward || back || left || right || up || down)) return null;

        if (up && !down) {
            return new Vec3d(0, 1, 0);
        } else if (down && !up) {
            return new Vec3d(0, -1, 0);
        }

        double forwardVal = (forward ? 1.0 : 0.0) - (back ? 1.0 : 0.0);
        double strafeVal  = (right ? 1.0 : 0.0) - (left ? 1.0 : 0.0);

        if (forwardVal == 0.0 && strafeVal == 0.0) return null;

        double lx = strafeVal;
        double lz = forwardVal;

        double yawRad = Math.toRadians(MC.player.getYaw());
        double fx = -Math.sin(yawRad);
        double fz =  Math.cos(yawRad);
        double rx = -Math.sin(yawRad + Math.PI / 2.0);
        double rz =  Math.cos(yawRad + Math.PI / 2.0);

        double wx = fx * lz + rx * lx;
        double wz = fz * lz + rz * lx;

        Vec3d worldDir = new Vec3d(wx, 0.0, wz);
        if (worldDir.lengthSquared() == 0.0) return null;
        return worldDir.normalize();
    }

    private boolean useItemAnywhere(Item item) {
        int hotbarSlot = getSlotInHotbar(item);

        if (hotbarSlot != -1) {
            int prevSlot = MC.player.getInventory().getSelectedSlot();
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(hotbarSlot);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prevSlot);
            return true;
        }

        int invSlot = getSlotInInventory(item);
        if (invSlot != -1) {
            int selectedHotbarIndex = MC.player.getInventory().getSelectedSlot();
            int containerInvSlot = convertSlot(invSlot);

            INVENTORY_MANAGER.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);
            return true;
        }

        return false;
    }

    private int getSlotInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int getSlotInInventory(Item item) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }
}
