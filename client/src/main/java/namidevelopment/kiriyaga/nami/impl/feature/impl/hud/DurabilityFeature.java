package namidevelopment.kiriyaga.nami.impl.feature.impl.hud;

import namidevelopment.kiriyaga.nami.impl.feature.HudElementFeature;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class DurabilityFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public DurabilityFeature() {
        super("Durability", "Displays item durability in main hand.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        Minecraft mc = MC;
        if (mc.player == null) return CAT_FORMAT.format("{bg}NaN");

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            width = FONT_SERVICE.getWidth("No item");
            height = FONT_SERVICE.getHeight();
            return CAT_FORMAT.format("{bg}No item");
        }

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamageValue();

        int durability = maxDamage - damage;
        double durabilityPercent = 100.0 * durability / maxDamage;

        String durabilityText = String.format("%d / %d (%.1f%%)", durability, maxDamage, durabilityPercent);

        String text;
        if (displayLabel.get()) {
            text = "{bg}Durability: {bw}" + durabilityText;
        } else {
            text = "{bw}" + durabilityText;
        }

        width = FONT_SERVICE.getWidth(text.replace("{bg}", "").replace("{bw}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}