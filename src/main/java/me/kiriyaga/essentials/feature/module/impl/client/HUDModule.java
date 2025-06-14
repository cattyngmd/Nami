package me.kiriyaga.essentials.feature.module.impl.client;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.Render2DEvent;
import me.kiriyaga.essentials.event.impl.PreTickEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import me.kiriyaga.essentials.util.ChatAnimationHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.*;
import java.text.Normalizer;

import static me.kiriyaga.essentials.Essentials.*;

public class HUDModule extends Module {

    public final IntSetting updateInterval = addSetting(new IntSetting("Update Interval", 1, 1, 20));

    public final BoolSetting watermarkEnabled = addSetting(new BoolSetting("Watermark", true));
    public final BoolSetting armorEnabled = addSetting(new BoolSetting("Armor", true));
    public final BoolSetting coordsEnabled = addSetting(new BoolSetting("Coordinates", true));
    public final BoolSetting facingEnabled = addSetting(new BoolSetting("Facing", true));
    public final BoolSetting fpsEnabled = addSetting(new BoolSetting("FPS", true));
    public final BoolSetting pingEnabled = addSetting(new BoolSetting("Ping", false));
    public final BoolSetting lagWarningEnabled = addSetting(new BoolSetting("Lag Warning", false));

    private int tickCounter = 0;
    private static final int PADDING = 2;

    private Text coordsText = Text.empty();
    private Text facingText = Text.empty();
    private Text watermarkText = Text.empty();
    private Text fpsText = Text.empty();
    private Text pingText = Text.empty();
    private boolean serverLagging = false;


    public HUDModule() {
        super("HUD","Displays in game hud.", Category.CLIENT, "ргв");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(PreTickEvent event) {
        tickCounter++;
        if (tickCounter % updateInterval.get() != 0) return;

        updateAllData();
    }

    private void updateAllData() {
        MinecraftClient mc = MINECRAFT;
        if (mc.world == null || mc.player == null) return;

        if (watermarkEnabled.get()) {
            watermarkText = Text.of(NAME + " " + VERSION);
        }

        if (coordsEnabled.get()) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();


            coordsText = formatFancyCoords(x, y, z);

        }

        if (facingEnabled.get()) {
            facingText = getFacingText(mc);
        }

        if (fpsEnabled.get()) {
            fpsText = Text.literal("FPS: ").setStyle(Style.EMPTY.withColor(getColorModule().getStyledPrimaryColor().getRGB()))
                    .append(Text.literal(Integer.toString(mc.getCurrentFps())).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        }

        if (pingEnabled.get()) {
            int ping = PING_MANAGER.getPing();
            pingText = Text.literal("Ping: ").setStyle(Style.EMPTY.withColor(getColorModule().getStyledPrimaryColor().getRGB()))
                    .append(Text.literal(ping < 0 ? "N/A" : Integer.toString(ping)).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));        }

        if (lagWarningEnabled.get()) {
            serverLagging = PING_MANAGER.isConnectionUnstable();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        MinecraftClient mc = MINECRAFT;
        if (mc.world == null || mc.player == null) return;

        Color styled = getColorModule().getStyledPrimaryColor();
        Color opaque = new Color(styled.getRed(), styled.getGreen(), styled.getBlue(), 255);
        int colorInt = opaque.getRGB();

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float rawOffset = ChatAnimationHelper.getAnimationOffset();

        float animationOffset = (float) (14f - (rawOffset/1.428571428571429));

        renderTopLeft(event, colorInt);

        renderBottomLeft(event, colorInt, screenHeight, animationOffset);

        renderBottomRight(event, colorInt, screenWidth, screenHeight, animationOffset);

        if (lagWarningEnabled.get() && serverLagging) {
            drawLagWarning(event, screenWidth, screenHeight);
        }

        if (armorEnabled.get()) {
            renderArmorHUD(event);
        }
    }

    private void renderTopLeft(Render2DEvent event, int color) {
        if (watermarkEnabled.get() && !watermarkText.getString().isEmpty()) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, watermarkText, PADDING, PADDING, color, true);
        }
    }

    private void renderBottomLeft(Render2DEvent event, int color, int screenHeight, float animationOffset) {
        int y = screenHeight - MINECRAFT.textRenderer.fontHeight - PADDING;
        y -= (int) animationOffset;

        if (coordsEnabled.get() && !coordsText.getString().isEmpty()) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, coordsText, PADDING, y, color, true);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }

        if (facingEnabled.get() && !facingText.getString().isEmpty()) {
            event.getDrawContext().drawText(MINECRAFT.textRenderer, facingText, PADDING, y, color, true);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }
    }

    private void renderBottomRight(Render2DEvent event, int color, int screenWidth, int screenHeight, float animationOffset) {
        int y = screenHeight - MINECRAFT.textRenderer.fontHeight;
        y -= (int) animationOffset;

        if (fpsEnabled.get() && !fpsText.getString().isEmpty()) {
            int width = MINECRAFT.textRenderer.getWidth(fpsText);
            event.getDrawContext().drawText(MINECRAFT.textRenderer, fpsText, screenWidth - width - PADDING, y, color, true);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }

        if (pingEnabled.get() && !pingText.getString().isEmpty()) {
            int width = MINECRAFT.textRenderer.getWidth(pingText);
            event.getDrawContext().drawText(MINECRAFT.textRenderer, pingText, screenWidth - width - PADDING, y, color, true);
            y -= MINECRAFT.textRenderer.fontHeight + 2;
        }
    }

    private void renderArmorHUD(Render2DEvent event) {
        MinecraftClient mc = MINECRAFT;
        if (mc.player == null) return;

        double x = mc.getWindow().getScaledWidth() / 2 + 7 * mc.getWindow().getScaleFactor();
        double y = mc.getWindow().getScaledHeight() - 28 * mc.getWindow().getScaleFactor();

        ItemStack[] armor = new ItemStack[]{
                mc.player.getEquippedStack(EquipmentSlot.HEAD),
                mc.player.getEquippedStack(EquipmentSlot.CHEST),
                mc.player.getEquippedStack(EquipmentSlot.LEGS),
                mc.player.getEquippedStack(EquipmentSlot.FEET)
        };

        for (int i = 0; i < armor.length; i++) {
            ItemStack stack = armor[i];
            if (stack.isEmpty()) continue;

            double armorX = x + i * 18;
            double armorY = y;

            event.getDrawContext().drawItem(stack, (int) armorX, (int) armorY);
            event.getDrawContext().drawStackOverlay(MINECRAFT.textRenderer, stack, (int) armorX, (int) armorY);

            if (stack.isDamageable()) {
                float durability = (stack.getMaxDamage() - (float) stack.getDamage()) / stack.getMaxDamage();
                int percent = Math.round(durability * 100);
                int r = (int) ((1 - durability) * 255);
                int g = (int) (durability * 255);

                String durabilityText = Integer.toString(percent);
                event.getDrawContext().drawText(
                        MINECRAFT.textRenderer,
                        durabilityText,
                        (int) (armorX + 1),
                        (int) (armorY - 10),
                        new Color(r, g, 0).getRGB(),
                        true
                );
            }
        }
    }



    private void drawLagWarning(Render2DEvent event, int screenWidth, int screenHeight) {
        String warningText = "Server is lagging!";
        int textWidth = MINECRAFT.textRenderer.getWidth(warningText);
        int textHeight = MINECRAFT.textRenderer.fontHeight;

        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight - textHeight) / 2;

        Color styled = getColorModule().getStyledPrimaryColor();
        Color opaque = new Color(styled.getRed(), styled.getGreen(), styled.getBlue(), 255);
        int colorInt = opaque.getRGB();

        event.getDrawContext().drawText(MINECRAFT.textRenderer, warningText, x, y, colorInt, true);
    }

    private Text formatFancyCoords(double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();

        boolean isNether = mc.world != null && mc.world.getRegistryKey() == World.NETHER;
        boolean isOverworld = mc.world != null && mc.world.getRegistryKey() == World.OVERWORLD;

        double xAlt = isNether ? x * 8 : x / 8;
        double zAlt = isNether ? z * 8 : z / 8;

        Color primary = getColorModule().getStyledPrimaryColor();

        Style primaryStyle = Style.EMPTY.withColor(primary.getRGB());
        Style grayStyle = Style.EMPTY.withColor(Formatting.GRAY);
        Style whiteStyle = Style.EMPTY.withColor(Formatting.WHITE);

        MutableText result = Text.literal("XYZ ").setStyle(primaryStyle);
        result.append(formatNumberStyled(x, whiteStyle, grayStyle));
        result.append(Text.literal(" ").setStyle(primaryStyle));
        result.append(formatNumberStyled(y, whiteStyle, grayStyle));
        result.append(Text.literal(" ").setStyle(primaryStyle));
        result.append(formatNumberStyled(z, whiteStyle, grayStyle));

        if (isOverworld || isNether) {
            result.append(Text.literal(" [").setStyle(primaryStyle));
            result.append(formatNumberStyled(xAlt, whiteStyle, grayStyle));
            result.append(Text.literal(", ").setStyle(primaryStyle));
            result.append(formatNumberStyled(zAlt, whiteStyle, grayStyle));
            result.append(Text.literal("]").setStyle(primaryStyle));
        }
        return result;
    }

    private MutableText formatNumberStyled(double val, Style digitStyle, Style separatorStyle) {
        String formatted = formatNumber(val); // "123.0"
        int dotIndex = formatted.indexOf('.');

        if (dotIndex == -1) {
            return Text.literal(formatted).setStyle(digitStyle);
        }

        MutableText text = Text.literal(formatted.substring(0, dotIndex)).setStyle(digitStyle);
        text.append(Text.literal(".").setStyle(separatorStyle));
        text.append(Text.literal(formatted.substring(dotIndex + 1)).setStyle(digitStyle));

        return text;
    }


    private String formatNumber(double val) {
        double rounded = Math.round(val * 10.0) / 10.0;
        return String.format("%.1f", rounded).replace(',', '.');
    }


    private Text getFacingText(MinecraftClient mc) {
        if (mc.player == null) return Text.literal("Invalid").formatted(Formatting.RED);

        int facingIndex = MathHelper.floor(mc.player.getYaw() * 4.0F / 360.0F + 0.5D) & 3;

        String dir = switch (facingIndex) {
            case 0 -> "South";
            case 1 -> "West";
            case 2 -> "North";
            case 3 -> "East";
            default -> "Invalid";
        };

        String axis = switch (facingIndex) {
            case 0 -> "+Z";
            case 1 -> "-X";
            case 2 -> "-Z";
            case 3 -> "+X";
            default -> "?";
        };

        MutableText text = Text.literal(dir).formatted(Formatting.byColorIndex(getColorModule().getStyledPrimaryColor().getRGB()));
        text.append(Text.literal(" [").formatted(Formatting.byColorIndex(getColorModule().getStyledPrimaryColor().getRGB())));
        text.append(Text.literal(axis).formatted(Formatting.WHITE));
        text.append(Text.literal("]").formatted(Formatting.byColorIndex(getColorModule().getStyledPrimaryColor().getRGB())));

        return text;
    }


    private ColorModule getColorModule() {
        return MODULE_MANAGER.getModule(ColorModule.class);
    }
}
