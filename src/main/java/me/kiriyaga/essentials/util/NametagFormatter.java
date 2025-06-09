package me.kiriyaga.essentials.util;

import me.kiriyaga.essentials.feature.module.impl.render.NametagsModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;

public class NametagFormatter {

    private final NametagsModule module;
    private final DecimalFormat df = new DecimalFormat("##");

    public NametagFormatter(NametagsModule module) {
        this.module = module;
    }

    public Text formatPlayer(PlayerEntity player) {
        Text name = Text.literal(player.getName().getString()).setStyle(getStyle());

        if (module.showHealth.get()) {
            Text health = getHealthText(player);
            return name.copy().append(" ").append(health);
        }

        return name;
    }

    private Text getHealthText(PlayerEntity player) {
        double hp = player.getHealth() + player.getAbsorptionAmount();
        int health = (int) Math.round(hp);

        Formatting color = switch (health) {
            case 19, 20 -> Formatting.GREEN;
            case 13, 14, 15, 16, 17, 18 -> Formatting.YELLOW;
            case 8, 9, 10, 11, 12 -> Formatting.GOLD;
            case 6, 7 -> Formatting.RED;
            default -> Formatting.DARK_RED;
        };

        return Text.literal(df.format(hp)).formatted(color).setStyle(getStyle());
    }


    public Text formatEntity(Entity entity) {
        return Text.literal(entity.getName().getString()).setStyle(getStyle());
    }

    public Text formatItem(ItemEntity item) {
        String name = item.getStack().getName().getString();
        int count = item.getStack().getCount();
        String text = count > 1 ? name + " x" + count : name;
        return Text.literal(text).setStyle(getStyle());
    }

    private String getHealthString(PlayerEntity player) {
        double hp = player.getHealth() + player.getAbsorptionAmount();
        int health = (int) Math.round(hp);

        Formatting color = switch (health) {
            case 19, 20 -> Formatting.GREEN;
            case 13, 14, 15, 16, 17, 18 -> Formatting.YELLOW;
            case 8, 9, 10, 11, 12 -> Formatting.GOLD;
            case 6, 7 -> Formatting.RED;
            default -> Formatting.DARK_RED;
        };

        return color + df.format(hp);
    }

    private Style getStyle() {
        boolean bold = module.formatting.get() == NametagsModule.TextFormat.Bold || module.formatting.get() == NametagsModule.TextFormat.Both;
        boolean italic = module.formatting.get() == NametagsModule.TextFormat.Italic || module.formatting.get() == NametagsModule.TextFormat.Both;
        return Style.EMPTY.withBold(bold).withItalic(italic);
    }
}
