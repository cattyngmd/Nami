package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.Nami;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule(category = "client")
public class HUDModule extends Module {

    public enum SpeedMode {
        kmh, bps
    }

    public final IntSetting updateInterval = addSetting(new IntSetting("interval", 1, 1, 20));

    public final BoolSetting chatAnimation = addSetting(new BoolSetting("chat animation", true));
    public final BoolSetting watermarkEnabled = addSetting(new BoolSetting("watermark", true));
    public final BoolSetting armorEnabled = addSetting(new BoolSetting("armor", true));
    public final BoolSetting armorDurability = addSetting(new BoolSetting("armor durability", true));
    public final BoolSetting speedEnabled = addSetting(new BoolSetting("speed", true));
    public final EnumSetting<SpeedMode> mode = addSetting(new EnumSetting<>("mode", SpeedMode.kmh));
    public final BoolSetting totemsEnabled = addSetting(new BoolSetting("totems", true));
    public final BoolSetting coordsEnabled = addSetting(new BoolSetting("coordinates", true));
    public final BoolSetting facingEnabled = addSetting(new BoolSetting("facing", true));
    public final BoolSetting fpsEnabled = addSetting(new BoolSetting("fps", true));
    public final BoolSetting pingEnabled = addSetting(new BoolSetting("ping", false));
    public final BoolSetting lagWarningEnabled = addSetting(new BoolSetting("lag warning", false));
    public final BoolSetting time = addSetting(new BoolSetting("time", false));
    public final BoolSetting greetingEnabled = addSetting(new BoolSetting("greeting", true));
    public final IntSetting greetingDelay = addSetting(new IntSetting("greeting delay", 30, 5, 120));
    public final BoolSetting shadow = addSetting(new BoolSetting("shadow", true));
    public final BoolSetting bounce = addSetting(new BoolSetting("bounce", false));
    public final IntSetting bounceSpeed = addSetting(new IntSetting("bounce speed", 5, 1, 20));
    public final IntSetting bounceIntensity = addSetting(new IntSetting("bounce intensity", 30, 10, 70));

    private int tickCounter = 0;
    private static final int PADDING = 1;
    private float bounceProgress = 0f;
    private boolean increasing = true;

    private Text coordsText = Text.empty();
    private Text facingText = Text.empty();
    private Text watermarkText = Text.empty();
    private Text fpsText = Text.empty();
    private Text pingText = Text.empty();
    private Text speedText = Text.empty();
    private Text timeText = Text.empty();
    private boolean serverLagging = false;
    private int primaryRGB;

    private static final int SPEED_SAMPLES = 80;
    private final double[] speedSamples = new double[SPEED_SAMPLES];
    private int speedSampleIndex = 0;
    private boolean speedBufferFilled = false;
    private double lastX = 0, lastY = 0, lastZ = 0;
    public double speed;

    private String fullGreeting = "";
    private int greetingCharIndex = 0;
    private int dotAnimationTimer = 0;
    private boolean dotVisible = true;
    private long lastWorldJoinTime = 0;
    private String currentGreeting = "";
    private boolean isFadingOut = false;
    private long greetingShownTime = 0;
    private final long greetingDisplayDuration = 5000;
    private static final String[] GREETINGS = {
            "Greetings %s :^)",
            "Looking great today %s :>",
            "Are ya winning son?",
            "Im like a pedo, but a good one :<",
            "Good evening %s :^)",
            "kids do not rename your obby",
            "OldLadyNorth did nothing wrong ^_^",
            "bro why are all these devs shooting estrogen into their morning monsters?",
            "Kiriyaga was here",
            "1.12.2 is just bad",
            "killaura pls dont kill my friends",
            "can we argue with the fact that future is a pedo client?",
            "Verify you are human...",
            "Script to win :^)",
            "dr donut buy mio client (c)",
            "Skidtrap has fallen to it's poetic end (c)",
            "Do not forget to drink water %s :^)",
            "今朝毎朝",
            "Kesa MaiAsa was here",
            "phobot is not real",
            "Do not forget to get some sleep, %s!"
    };


    public HUDModule() {
        super("hud","Displays in game hud.", ModuleCategory.of("client"), "ргв");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdate(PreTickEvent event) {
        if (bounce.get()) {
            float step = bounceSpeed.get() / 100f;
            if (increasing) {
                bounceProgress += step;
                if (bounceProgress >= 1f) {
                    bounceProgress = 1f;
                    increasing = false;
                }
            } else {
                bounceProgress -= step;
                if (bounceProgress <= 0f) {
                    bounceProgress = 0f;
                    increasing = true;
                }
            }
        } else {
            bounceProgress = 0f;
        }

        tickCounter++;
        if (tickCounter % updateInterval.get() != 0) return;

        updateAllData();
    }


    private void updateAllData() {
        MinecraftClient mc = Nami.MC;
        if (mc.world == null || mc.player == null) return;

        Color styled = getColorModule().getStyledGlobalColor();
        int rawColor = styled.getRGB();
        int rgb = rawColor & 0x00FFFFFF;
        primaryRGB = 0xFF000000 | rgb;

        int pulsingPrimary = getPulsingColor(primaryRGB);
        int pulsingWhite = getPulsingColor(0xFFFFFFFF);

        if (watermarkEnabled.get()) {
            watermarkText = Text.of(DISPLAY_NAME + " " + VERSION);
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
            fpsText = Text.literal("FPS: ").setStyle(Style.EMPTY.withColor(pulsingPrimary))
                    .append(Text.literal(Integer.toString(mc.getCurrentFps())).setStyle(Style.EMPTY.withColor(pulsingWhite)));
        }

        if (pingEnabled.get()) {
            int ping = PING_MANAGER.getPing();
            pingText = Text.literal("Ping: ").setStyle(Style.EMPTY.withColor(pulsingPrimary))
                    .append(Text.literal(ping < 0 ? "N/A" : ping + "ms").setStyle(Style.EMPTY.withColor(pulsingWhite)));
        }

        if (lagWarningEnabled.get()) {
            serverLagging = PING_MANAGER.isConnectionUnstable();
        }

        if (speedEnabled.get()) {
            double dx = mc.player.getX() - lastX;
            double dz = mc.player.getZ() - lastZ;

            double instantSpeed = Math.sqrt(dx * dx + dz * dz) * 20;

            speedSamples[speedSampleIndex] = instantSpeed;
            speedSampleIndex = (speedSampleIndex + 1) % SPEED_SAMPLES;

            if (speedSampleIndex == 0) speedBufferFilled = true;

            int count = speedBufferFilled ? SPEED_SAMPLES : speedSampleIndex;
            double sum = 0;
            for (int i = 0; i < count; i++) {
                sum += speedSamples[i];
            }
            double averageSpeed = count > 0 ? sum / count : 0;
            speed = averageSpeed;

            String speedStr;
            if (mode.get() == SpeedMode.bps) {
                speedStr = formatSpeedNumber(averageSpeed) + " bp/s";
            } else if (mode.get() == SpeedMode.kmh) {
                double kmh = averageSpeed * 3.6;
                speedStr = formatSpeedNumber(kmh) + " km/h";
            } else {
                speedStr = formatSpeedNumber(averageSpeed);
            }

            speedText = Text.literal("Speed: ")
                    .setStyle(Style.EMPTY.withColor(pulsingPrimary))
                    .append(Text.literal(speedStr).setStyle(Style.EMPTY.withColor(pulsingWhite)));


            lastX = mc.player.getX();
            lastY = mc.player.getY();
            lastZ = mc.player.getZ();
        }

        if (time.get()) {
            LocalDateTime now = LocalDateTime.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            String formattedTime = now.format(formatter);

            timeText = Text.literal(formattedTime);
        }

        if (greetingEnabled.get()) {
            if (lastWorldJoinTime == 0) {
                lastWorldJoinTime = System.currentTimeMillis();
            } else {
                long elapsedSeconds = (System.currentTimeMillis() - lastWorldJoinTime) / 1000;
                if (elapsedSeconds >= greetingDelay.get() && fullGreeting.isEmpty()) {
                    String greetingTemplate = GREETINGS[(int)(Math.random() * GREETINGS.length)];
                    fullGreeting = String.format(greetingTemplate, Nami.MC.player.getName().getString());
                    greetingCharIndex = 0;
                    greetingShownTime = 0;
                }
            }

            if (!fullGreeting.isEmpty()) {
                if (greetingCharIndex < fullGreeting.length() && !isFadingOut) {
                    greetingCharIndex++;
                    currentGreeting = fullGreeting.substring(0, greetingCharIndex);
                } else if (greetingCharIndex == fullGreeting.length() && !isFadingOut) {
                    if (greetingShownTime == 0) {
                        greetingShownTime = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - greetingShownTime >= greetingDisplayDuration) {
                        isFadingOut = true;
                        greetingShownTime = 0;
                    }
                } else if (isFadingOut) {
                    if (greetingCharIndex > 0) {
                        greetingCharIndex--;
                        currentGreeting = fullGreeting.substring(0, greetingCharIndex);
                    } else {
                        fullGreeting = "";
                        currentGreeting = "";
                        isFadingOut = false;
                        greetingShownTime = 0;
                        lastWorldJoinTime = System.currentTimeMillis();
                    }
                }
            }

            dotAnimationTimer++;
            if (dotAnimationTimer >= 20) {
                dotAnimationTimer = 0;
                dotVisible = !dotVisible;
            }

        } else {
            lastWorldJoinTime = 0;
            fullGreeting = "";
            greetingCharIndex = 0;
            currentGreeting = "";
            greetingShownTime = 0;
            isFadingOut = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender2D(Render2DEvent event) {
        MinecraftClient mc = Nami.MC;
        if (mc.world == null || mc.player == null || mc.getDebugHud().shouldShowDebugHud()) return;

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float animationOffset = ChatAnimationHelper.getAnimationOffset();

        int pulsingPrimary = getPulsingColor(primaryRGB);

        renderTopLeft(event, pulsingPrimary);

        renderBottomLeft(event, pulsingPrimary, screenHeight, animationOffset);

        renderBottomRight(event, pulsingPrimary, screenWidth, screenHeight, animationOffset);

        if (lagWarningEnabled.get() && serverLagging) {
            drawLagWarning(event, screenWidth, screenHeight);
        }

        if (armorEnabled.get()) {
            renderArmorHUD(event);
        }

        if (totemsEnabled.get()) {
            renderTotems(event, screenWidth, screenHeight);
        }

        if (greetingEnabled.get()){
            renderGreeting(event, screenWidth);
        }
    }

    private void renderTopLeft(Render2DEvent event, int color) {
        int x = PADDING;

        if (watermarkEnabled.get() && !watermarkText.getString().isEmpty()) {
            event.getDrawContext().drawText(
                    MC.textRenderer,
                    watermarkText,
                    x,
                    PADDING,
                    color,
                    shadow.get()
            );

            int watermarkWidth = MC.textRenderer.getWidth(watermarkText);
            int gap = 5;
            x += watermarkWidth + gap;
        }

        if (time.get() && timeText != null) {
            int pulsingColor = getPulsingColor(0xFFB2BAAB);
            event.getDrawContext().drawText(
                    MC.textRenderer,
                    timeText,
                    x,
                    PADDING,
                    pulsingColor,
                    shadow.get()
            );
        }
    }

    private void renderBottomLeft(Render2DEvent event, int color, int screenHeight, float animationOffset) {
        int y = screenHeight - MC.textRenderer.fontHeight - PADDING;
        y -= (int) animationOffset;

        if (coordsEnabled.get() && !coordsText.getString().isEmpty()) {
            event.getDrawContext().drawText(MC.textRenderer, coordsText, PADDING, y, color, shadow.get());
            y -= MC.textRenderer.fontHeight + 2;
        }

        if (facingEnabled.get() && !facingText.getString().isEmpty()) {
            event.getDrawContext().drawText(MC.textRenderer, facingText, PADDING, y, color, shadow.get());
            y -= MC.textRenderer.fontHeight + 2;
        }
    }

    private void renderBottomRight(Render2DEvent event, int color, int screenWidth, int screenHeight, float animationOffset) {
        int y = screenHeight - MC.textRenderer.fontHeight;
        y -= (int) animationOffset;

        if (speedEnabled.get() && !speedText.getString().isEmpty()) {
            int width = MC.textRenderer.getWidth(speedText);
            event.getDrawContext().drawText(MC.textRenderer, speedText, screenWidth - width - PADDING, y, color, shadow.get());
            y -= MC.textRenderer.fontHeight + 2;
        }

        if (pingEnabled.get() && !pingText.getString().isEmpty()) {
            int width = MC.textRenderer.getWidth(pingText);
            event.getDrawContext().drawText(MC.textRenderer, pingText, screenWidth - width - PADDING, y, color, shadow.get());
            y -= MC.textRenderer.fontHeight + 2;
        }

        if (fpsEnabled.get() && !fpsText.getString().isEmpty()) {
            int width = MC.textRenderer.getWidth(fpsText);
            event.getDrawContext().drawText(MC.textRenderer, fpsText, screenWidth - width - PADDING, y, color, shadow.get());
            y -= MC.textRenderer.fontHeight + 2;
        }
    }

    private void renderArmorHUD(Render2DEvent event) {
        MinecraftClient mc = Nami.MC;
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
            event.getDrawContext().drawStackOverlay(Nami.MC.textRenderer, stack, (int) armorX, (int) armorY);

            if (stack.isDamageable() && armorDurability.get()) {
                float durability = (stack.getMaxDamage() - (float) stack.getDamage()) / stack.getMaxDamage();
                int percent = Math.round(durability * 100);
                int r = (int) ((1 - durability) * 255);
                int g = (int) (durability * 255);

                String durabilityText = percent + "%";

                Matrix3x2fStack matrices = event.getDrawContext().getMatrices();

                matrices.pushMatrix();

                float scale = 0.7f;
                matrices.scale(scale, scale);

                float scaledX = (float) ((armorX + 1) / scale);
                float scaledY = (float) ((armorY - 10) / scale);

                int baseColor = new Color(r, g, 0).getRGB();
                int pulsingColor = getPulsingColor(baseColor);

                event.getDrawContext().drawText(
                        Nami.MC.textRenderer,
                        durabilityText,
                        (int) scaledX,
                        (int) scaledY,
                        pulsingColor,
                        shadow.get()
                );

                matrices.popMatrix();
            }
        }
    }


    private void renderTotems(Render2DEvent event, int screenWidth, int screenHeight) {
        MinecraftClient mc = Nami.MC;
        if (mc.player == null) return;

        int totemCount = 0;

        for (ItemStack stack : mc.player.getInventory().getMainStacks()) {
            if (stack.getItem() == net.minecraft.item.Items.TOTEM_OF_UNDYING) {
                totemCount += stack.getCount();
            }
        }

        ItemStack offHandStack = mc.player.getOffHandStack();
        if (offHandStack.getItem() == net.minecraft.item.Items.TOTEM_OF_UNDYING) {
            totemCount += offHandStack.getCount();
        }

        // if (totemCount == 0) return;

        int y = screenHeight - 52;

        int xCenter = screenWidth / 2;

        int iconSize = 16;
        int iconX = xCenter - iconSize / 2;
        int iconY = y;

        ItemStack totemStack = new ItemStack(net.minecraft.item.Items.TOTEM_OF_UNDYING);

        event.getDrawContext().drawItem(totemStack, iconX, iconY);

        String countStr = String.valueOf(totemCount);

        int textX = iconX + iconSize - 4;
        int textY = iconY + iconSize - 6;

        int baseColor = 0xFFFFFFFF;
        int pulsingColor = getPulsingColor(baseColor);

        event.getDrawContext().drawText(
                mc.textRenderer,
                Text.literal(countStr).setStyle(Style.EMPTY.withColor(pulsingColor)),
                textX,
                textY,
                pulsingColor,
                true
        );
    }

    private void drawLagWarning(Render2DEvent event, int screenWidth, int screenHeight) {
        String warningText = "Server is not responding in " + PING_MANAGER.getConnectionUnstableTimeSeconds() + "s";
        int textWidth = MC.textRenderer.getWidth(warningText);
        int textHeight = MC.textRenderer.fontHeight;

        int x = (screenWidth - textWidth) / 2;
        int y = ((screenHeight - textHeight) / 2) - 60;

        int pulsingColor = getPulsingColor(primaryRGB);
        event.getDrawContext().drawText(MC.textRenderer, warningText, x, y, pulsingColor, shadow.get());
    }

    private Text formatFancyCoords(double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();

        boolean isNether = mc.world != null && mc.world.getRegistryKey() == World.NETHER;
        boolean isOverworld = mc.world != null && mc.world.getRegistryKey() == World.OVERWORLD;

        double xAlt = isNether ? x * 8 : x / 8;
        double zAlt = isNether ? z * 8 : z / 8;

        int pulsingPrimaryColor = getPulsingColor(primaryRGB);
        Style primaryStyle = Style.EMPTY.withColor(pulsingPrimaryColor);
        Style grayStyle = Style.EMPTY.withColor(getPulsingColor(0xFFB2BAAB));
        Style whiteStyle = Style.EMPTY.withColor(getPulsingColor(0xFFFFFFFF));


        MutableText result = Text.literal("XYZ ").setStyle(primaryStyle);
        result.append(formatNumberStyled(x, whiteStyle, grayStyle));
        result.append(Text.literal(", ").setStyle(primaryStyle));
        result.append(formatNumberStyled(y, whiteStyle, grayStyle));
        result.append(Text.literal(", ").setStyle(primaryStyle));
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
        String formatted = formatNumber(val); // -123.4
        int dotIndex = formatted.indexOf('.');

        MutableText text = Text.literal("");

        Style pulsingDigitStyle = Style.EMPTY.withColor(getPulsingColor(0xFFFFFFFF));

        if (formatted.startsWith("-")) {
            text.append(Text.literal("-").setStyle(separatorStyle));
            formatted = formatted.substring(1);
            dotIndex--;
        }

        if (dotIndex == -1) {
            return text.append(Text.literal(formatted).setStyle(pulsingDigitStyle));
        }

        text.append(Text.literal(formatted.substring(0, dotIndex)).setStyle(pulsingDigitStyle));
        text.append(Text.literal(".").setStyle(separatorStyle));
        text.append(Text.literal(formatted.substring(dotIndex + 1)).setStyle(pulsingDigitStyle));

        return text;
    }

    private String formatNumber(double val) {
        double rounded = Math.round(val * 10.0) / 10.0;
        return String.format("%.1f", rounded).replace(',', '.');
    }

    private String formatSpeedNumber(double val) {
        double rounded = Math.round(val * 10.0) / 10.0;
        return String.format("%.2f", rounded).replace(',', '.');
    }

    private Text getFacingText(MinecraftClient mc) {
        if (mc.player == null) return Text.literal("Invalid").setStyle(Style.EMPTY.withColor(Formatting.RED));

        float yaw = mc.player.getYaw() % 360;
        if (yaw < 0) yaw += 360;

        double rad = Math.toRadians(yaw);

        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);

        double absDx = Math.abs(dx);
        double absDz = Math.abs(dz);

        int pulsingColor = getPulsingColor(primaryRGB);
        Style primaryStyle = Style.EMPTY.withColor(pulsingColor);

        Style whiteStyle = Style.EMPTY.withColor(getPulsingColor(0xFFFFFFFF));
        Style grayStyle = Style.EMPTY.withColor(getPulsingColor(0xFFB2BAAB));

        String dir = switch ((int) Math.floor((yaw + 45) / 90) % 4) {
            case 0 -> "South";
            case 1 -> "West";
            case 2 -> "North";
            case 3 -> "East";
            default -> "Invalid";
        };

        MutableText text = Text.literal(dir).setStyle(primaryStyle);
        text.append(Text.literal(" [").setStyle(primaryStyle));

        BiFunction<String, Style, MutableText> formatAxis = (axis, color) -> {
            MutableText t = Text.literal("");
            if (axis.startsWith("+") || axis.startsWith("-")) {
                t.append(Text.literal(axis.substring(0, 1)).setStyle(grayStyle));
                t.append(Text.literal(axis.substring(1)).setStyle(color));
            } else {
                t.append(Text.literal(axis).setStyle(color));
            }
            return t;
        };

        if (absDx > 0.2 && absDz > 0.2 && Math.abs(absDx - absDz) < 0.4) {
            String axis1 = dz > 0 ? "+Z" : "-Z";
            String axis2 = dx > 0 ? "+X" : "-X";

            text.append(formatAxis.apply(axis1, whiteStyle));
            text.append(Text.literal(",").setStyle(primaryStyle));
            text.append(Text.literal(" "));
            text.append(formatAxis.apply(axis2, whiteStyle));
        } else {
            if (absDz > absDx) {
                String axis = dz > 0 ? "+Z" : "-Z";
                text.append(formatAxis.apply(axis, whiteStyle));
            } else {
                String axis = dx > 0 ? "+X" : "-X";
                text.append(formatAxis.apply(axis, whiteStyle));
            }
        }

        text.append(Text.literal("]").setStyle(primaryStyle));

        return text;
    }

    private void renderGreeting(Render2DEvent event, int screenWidth) {
        if (!greetingEnabled.get() || currentGreeting.isEmpty()) return;

        String textToRender = currentGreeting + (dotVisible ? "." : "");
        int textWidth = MC.textRenderer.getWidth(textToRender);
        int x = (screenWidth - textWidth) / 2;
        int y = PADDING;

        int pulsingColor = getPulsingColor(primaryRGB);
        event.getDrawContext().drawText(
                MC.textRenderer,
                textToRender,
                x,
                y,
                pulsingColor,
                shadow.get()
        );
    }

    private int getPulsingColor(int originalColor) {
        if (!bounce.get()) return originalColor;

        float intensity = bounceIntensity.get() / 100f;
        float pulseFactor = (float) Math.sin(bounceProgress * Math.PI);
        int a = (originalColor >> 24) & 0xFF;
        int r = (originalColor >> 16) & 0xFF;
        int g = (originalColor >> 8) & 0xFF;
        int b = originalColor & 0xFF;

        int minAlpha = (int)(a * (1 - intensity));
        int pulsingAlpha = minAlpha + (int)((a - minAlpha) * pulseFactor);

        return (pulsingAlpha << 24) | (r << 16) | (g << 8) | b;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }
}
