package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class DurabilityModule extends HudElementModule {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("display label", true));

    public DurabilityModule() {
        super("durability", "Displays item durability in main hand.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        MinecraftClient mc = MC;
        if (mc.player == null) return CAT_FORMAT.format("{bg}NaN");

        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty() || !stack.isDamageable()) {
            width = MC.textRenderer.getWidth("No item");
            height = MC.textRenderer.fontHeight;
            return CAT_FORMAT.format("{bg}No item");
        }

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamage();

        int durability = maxDamage - damage;
        double durabilityPercent = 100.0 * durability / maxDamage;

        String durabilityText = String.format("%d / %d (%.1f%%)", durability, maxDamage, durabilityPercent);

        String text;
        if (displayLabel.get()) {
            text = "{bg}Durability: {bw}" + durabilityText;
        } else {
            text = "{bw}" + durabilityText;
        }

        width = MC.textRenderer.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = MC.textRenderer.fontHeight;

        return CAT_FORMAT.format(text);
    }
}