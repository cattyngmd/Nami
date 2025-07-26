package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.MoveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.core.RotationManager;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ElytraFlyModule extends Module {

    public enum FlyMode {
        none, bounce
    }

    public final EnumSetting<FlyMode> mode = addSetting(new EnumSetting<>("mode", FlyMode.bounce));
    private final BoolSetting pitch = addSetting(new BoolSetting("pitch", true));
    private final BoolSetting boost = addSetting(new BoolSetting("boost", false));
    private final BoolSetting newBoost = addSetting(new BoolSetting("new boost", false));
    private final BoolSetting autoWalkEnable = addSetting(new BoolSetting("auto walk enable", true));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    public ElytraFlyModule() {
        super("elytra fly", "Improves elytra flying.", ModuleCategory.of("movement"), "уднекфадн", "elytrafly");
    }

    @Override
    public void onEnable() {
        if (autoWalkEnable.get() && !MODULE_MANAGER.getStorage().getByClass(AutoWalkModule.class).isEnabled()) {
            MODULE_MANAGER.getStorage().getByClass(AutoWalkModule.class).toggle();
        }
    }

    @Override
    public void onDisable() {
        if (autoWalkEnable.get() && MODULE_MANAGER.getStorage().getByClass(AutoWalkModule.class).isEnabled()) {
            MODULE_MANAGER.getStorage().getByClass(AutoWalkModule.class).toggle();
        }
        setJumpHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onPreTick(PreTickEvent event) {
        if (MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        if (mode.get() == FlyMode.bounce) {

            if (boost.get() && MC.player.getVelocity().y > 0) {
                MC.player.setVelocity(MC.player.getVelocity().x, 0.0, MC.player.getVelocity().z);
            }

            setJumpHeld(true);

            //75 magic value = its just the best value
            if (pitch.get())
                ROTATION_MANAGER.submitRequest(new RotationManager.RotationRequest(this.getName(), rotationPriority.get(), MC.player.getYaw(), 75.00f));

            MC.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onMove(MoveEvent event) {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        if (!player.isOnGround()) return;

        double currentBps = Math.sqrt(player.getVelocity().x * player.getVelocity().x + player.getVelocity().z * player.getVelocity().z);

        if (player.isSprinting() && currentBps > 9.00 && newBoost.get()) {

            Vec3d velocity = player.getVelocity();
            event.setMovement(new Vec3d(velocity.x, 0, velocity.z));
        }
    }

    private void setJumpHeld(boolean held) {
        KeyBinding jumpKey = MC.options.jumpKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) jumpKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        jumpKey.setPressed(physicallyPressed || held);
    }
}
