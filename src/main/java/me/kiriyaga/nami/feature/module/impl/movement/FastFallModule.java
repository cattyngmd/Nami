package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import net.minecraft.world.phys.Vec3;

import static me.kiriyaga.nami.Nami.INPUT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class FastFallModule extends Module {

    private final DoubleSetting speed = addSetting(new DoubleSetting("Speed", 1.00, 0.10, 5.00));

    public FastFallModule() {
        super("FastFall", "Fall from blocks faster.", ModuleCategory.of("Movement"), "fastfall");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onTick(PreTickEvent ev) {
        if (MC.level == null || MC.player == null) return;

        if (!INPUT_MANAGER.hasAnyInput() || INPUT_MANAGER.isJumpPressed() || MC.player.isInPowderSnow || MC.player.isUnderWater() || MC.player.isInLava() || MC.player.isInWater() || MC.player.isFallFlying() || MC.player.isNoGravity())
            return;

        if (MC.player.onGround()) {
            Vec3 prev = MC.player.getDeltaMovement();
            MC.player.setDeltaMovement(prev.x, -speed.get(), prev.z);
        }
    }
}
