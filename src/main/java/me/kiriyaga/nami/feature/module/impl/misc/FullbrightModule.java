package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class FullbrightModule extends Module {

    public enum Mode {
        GAMMA, POTION
    }

    public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("mode", Mode.GAMMA));
    public final DoubleSetting amount = addSetting(new DoubleSetting("amount", 2, 1, 25));

    private double defaultGamma;

    public FullbrightModule() {
        super("fullbright", "Modifies your game brightness", ModuleCategory.of("misc"), "autogamma", "gamma", "autogmam", "фгещпфььф");
        amount.setShowCondition(() -> mode.get() == Mode.GAMMA);
        defaultGamma = MC.options.getGamma().getValue();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onTick(PostTickEvent ev) {
        if (mode.get() == Mode.GAMMA) {
            double current = MC.options.getGamma().getValue();
            if (current != amount.get()) {
                ((ISimpleOption) (Object) MC.options.getGamma()).setValue(amount.get());
            }

            if (MC.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                MC.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }

        } else if (mode.get() == Mode.POTION) {
            if (MC.options.getGamma().getValue() != defaultGamma) {
                ((ISimpleOption) (Object) MC.options.getGamma()).setValue(defaultGamma);
            }

            MC.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ((ISimpleOption) (Object) MC.options.getGamma()).setValue(defaultGamma);
        if (MC.player != null && MC.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            MC.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }
}