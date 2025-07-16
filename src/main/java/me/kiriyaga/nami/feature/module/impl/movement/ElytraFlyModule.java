package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.MoveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.HUDModule;
import me.kiriyaga.nami.manager.RotationManager;
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

public class ElytraFlyModule extends Module {

    public enum FlyMode {
        none, bounce
    }

    public final EnumSetting<FlyMode> mode = addSetting(new EnumSetting<>("mode", FlyMode.bounce));
    private final BoolSetting pitch = addSetting(new BoolSetting("pitch", true));
    private final BoolSetting boost = addSetting(new BoolSetting("boost", false));
    private final BoolSetting newBoost = addSetting(new BoolSetting("new boost", false));
    private final DoubleSetting targetSpeed = addSetting(new DoubleSetting("boost speed", 100.00, 40.00, 250.00));
    private final BoolSetting autoWalkEnable = addSetting(new BoolSetting("auto walk enable", true));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    public ElytraFlyModule() {
        super("elytra fly", "Improves elytra flying.", Category.movement, "уднекфадн", "elytrafly");
    }

    @Override
    public void onEnable() {
        if (autoWalkEnable.get() && !MODULE_MANAGER.getModule(AutoWalkModule.class).isEnabled()) {
            MODULE_MANAGER.getModule(AutoWalkModule.class).toggle();
        }
    }

    @Override
    public void onDisable() {
        if (autoWalkEnable.get() && MODULE_MANAGER.getModule(AutoWalkModule.class).isEnabled()) {
            MODULE_MANAGER.getModule(AutoWalkModule.class).toggle();
        }
        setJumpHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onPreTick(PreTickEvent event) {
        if (MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        if (mode.get() == FlyMode.bounce) {

            if (boost.get() && MC.player.getVelocity().y > 0 && MODULE_MANAGER.getModule(HUDModule.class).speed < targetSpeed.get()) {
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

        HUDModule hud = MODULE_MANAGER.getModule(HUDModule.class);
        double currentBps = hud.speed;

        if (player.isSprinting()
                && currentBps < targetSpeed.get()
                && currentBps > 60.0
                && newBoost.get()) {

            Vec3d velocity = player.getVelocity();
            event.setMovement(new Vec3d(velocity.x, 0, velocity.z));
            // event.cancel(); // если хочешь полностью перехватить движение
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
