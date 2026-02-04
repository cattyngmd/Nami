package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterFeature
public class DurabilityFeature extends HudElementFeature {

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));

    public DurabilityFeature() {
        super("Durability", "Displays item durability in main hand.", 0, 0, 50, 9);
    }

    @Override
    public Component getDisplayText() {
        Minecraft mc = MC;
        if (mc.player == null) return CAT_FORMAT.format("{global}NaN");

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            width = FONT_SERVICE.getWidth("No item");
            height = FONT_SERVICE.getHeight();
            return CAT_FORMAT.format("{global}No item");
        }

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamageValue();

        int durability = maxDamage - damage;
        double durabilityPercent = 100.0 * durability / maxDamage;

        String durabilityText = String.format("%d / %d (%.1f%%)", durability, maxDamage, durabilityPercent);

        String text;
        if (displayLabel.get()) {
            text = "{global}Durability: {white}" + durabilityText;
        } else {
            text = "{white}" + durabilityText;
        }

        width = FONT_SERVICE.getWidth(text.replace("{global}", "").replace("{white}", ""));
        height = FONT_SERVICE.getHeight();

        return CAT_FORMAT.format(text);
    }
}