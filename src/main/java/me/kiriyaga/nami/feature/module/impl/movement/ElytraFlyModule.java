package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.core.rotation.*;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ElytraFlyModule extends Module {

    public enum FlyMode {
        NONE, BOUNCE
    }

    public final EnumSetting<FlyMode> mode = addSetting(new EnumSetting<>("mode", FlyMode.BOUNCE));
    private final BoolSetting pitch = addSetting(new BoolSetting("pitch", true));
    private final IntSetting pitchDegree = addSetting(new IntSetting("pitch", 75, 0, 90));
    private final BoolSetting autoWalkEnable = addSetting(new BoolSetting("auto walk enable", true));
    private final IntSetting rotationPriority = addSetting(new IntSetting("rotation", 3, 1, 10));

    private double speed = 0;
    private double[] speedSamples = new double[]{25};
    private int speedSampleIndex = 0;
    private boolean speedBufferFilled = false;

    private double lastX = 0;
    private double lastZ = 0;

    public ElytraFlyModule() {
        super("elytra fly", "Improves elytra flying.", ModuleCategory.of("movement"), "уднекфадн", "elytrafly");
        pitch.setShowCondition(() -> mode.get() == FlyMode.BOUNCE);
        pitchDegree.setShowCondition(() -> mode.get() == FlyMode.BOUNCE && pitch.get());
        autoWalkEnable.setShowCondition(() -> mode.get() == FlyMode.BOUNCE);
        rotationPriority.setShowCondition(() -> mode.get() == FlyMode.BOUNCE);
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

        this.setDisplayInfo(mode.get().toString());

        if (mode.get() == FlyMode.BOUNCE) {

            setJumpHeld(true);

            if (pitch.get())
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(this.getName(), rotationPriority.get(), MC.player.getYaw(), pitchDegree.get().floatValue()));

            MC.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onTickSpeed(PreTickEvent event) {
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
}
