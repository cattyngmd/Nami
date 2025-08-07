package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.core.rotation.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ItemUseSlowEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.render.entity.LivingEntityRenderer;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.ROTATION_MANAGER;

@RegisterModule
public class NoSlowModule extends Module {
    public enum SlowMode {
        VANILLA, PARTIAL, ACCEL
    }

    public final EnumSetting<SlowMode> mode = addSetting(new EnumSetting<>("mode", SlowMode.ACCEL));
    private final BoolSetting onlyOnGround = addSetting(new BoolSetting("only on ground", true));


    public NoSlowModule() {
        super("no slow", "Reduces slowdown effect caused on player.", ModuleCategory.of("movement"), "тщыдщц");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onSlow(ItemUseSlowEvent ev){
        if (MC.player == null || MC.world == null || !MC.player.isUsingItem() || MC.player.isGliding() || MC.player.isRiding())
            return;

        if (onlyOnGround.get() && !MC.player.isOnGround())
            return;

        if (mode.get() == SlowMode.VANILLA){
            ev.cancel();
            return;
        }

        boolean boost = true; //cattyngmd
        if (mode.get() == SlowMode.ACCEL){
            boost = MC.player.age % 3 == 0 || MC.player.age % 4 == 0;
            //if (MC.player.age % 12 == 0) boost = false;

            if (boost){
                ev.cancel();
                return;
            }
        }

        if (mode.get() == SlowMode.PARTIAL) {
            if (MC.player.age % 2 == 0) {
                ev.cancel();
                return;
            }
        }
    }
}
