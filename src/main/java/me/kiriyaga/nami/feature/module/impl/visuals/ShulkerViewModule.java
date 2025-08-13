/*
 Originally made by @cattyngmd
 https://github.com/cattyngmd/shulker-view
 MIT (2024)
*/
package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.*;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.setting.impl.ColorSetting;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class ShulkerViewModule extends Module {

    public final BoolSetting tooltip = addSetting(new BoolSetting("tooltip", true));
    public final BoolSetting compact = addSetting(new BoolSetting("compact", true));
    public final BoolSetting bothSides = addSetting(new BoolSetting("both sides", true));
    public final BoolSetting borders = addSetting(new BoolSetting("borders", true));
    public final DoubleSetting scale = addSetting(new DoubleSetting("scale", 1, 0.5, 1.5));
    public final DoubleSetting scrollsensitivity = addSetting(new DoubleSetting("sensitivity", 1, 0.5, 3));

    private final List<ShulkerInfo> shulkerList = new ArrayList<>();
    private final int GRID_WIDTH = 20;
    private final int GRID_HEIGHT = 18;
    private final int MARGIN = 2;

    private int currentY = 0;
    private int startX = 0;
    private int offset = 0;
    private int totalHeight = 0;

    private double clickedX = -1, clickedY = -1;

    public ShulkerViewModule() {
        super("shulker view", "Improves shulker managment. Author @cattyngmd", ModuleCategory.of("visuals"),"shulkerview", "ыргдлукмшуц");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTick(PreTickEvent event) {
        if (!(MC.currentScreen instanceof HandledScreen<?> screen)) return;

        shulkerList.clear();
        for (Slot slot : screen.getScreenHandler().slots) {
            ShulkerInfo info = ShulkerInfo.create(slot.getStack(), slot.id, compact.get());
            if (info != null) shulkerList.add(info);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender(RenderScreenEvent event) {
        //CHAT_MANAGER.sendRaw("event called");

        if (!(MC.currentScreen instanceof HandledScreen)) return;

        //CHAT_MANAGER.sendRaw("current screen = " + MC.currentScreen.getClass().getName());
        //CHAT_MANAGER.sendRaw("total shulkers to render = " + shulkerList.size());

        DrawContext context = event.getDrawContext();
        boolean right = false;
        currentY = bothSides.get() ? MARGIN : MARGIN + offset;
        startX = MARGIN;
        float scale = this.scale.get().floatValue();

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        for (ShulkerInfo info : shulkerList) {
            int rows = info.rows();
            int cols = info.cols();

            int width = cols * GRID_WIDTH + MARGIN * cols;
            int height = rows * GRID_HEIGHT + MARGIN * rows;

            if (currentY + height > MC.getWindow().getScaledHeight() / scale && bothSides.get() && !right) {
                right = true;
                currentY = MARGIN + offset;
            }

            if (right) {
                float totalWidth = cols * GRID_WIDTH + MARGIN * cols;
                startX = (int) ((MC.getWindow().getScaledWidth() - totalWidth - MARGIN) / scale);
            }

           // CHAT_MANAGER.sendRaw("rendering shulker at (" + startX + ", " + currentY + "), size = " + cols + "x" + rows);

            context.fill(startX, currentY, startX + width, currentY + height, new Color(0, 0, 0, 75).getRGB());

            if (borders.get()){
                int borderColor = getShulkerColor(info.shulker);
                drawBorder(context, startX, currentY, width, height, borderColor);
            }

            int count = 0;
            for (ItemStack stack : info.stacks) {
                if (compact.get() && stack.isEmpty()) break;
                int x = startX + (count % info.cols) * GRID_WIDTH + MARGIN;
                int y = currentY + (count / info.cols) * GRID_HEIGHT + MARGIN;

                context.drawItem(stack, x, y);
                context.drawStackOverlay(MC.textRenderer, stack, x, y, null);

                if (tooltip.get() && !stack.isEmpty() && isHovered(event.getMouseX(), event.getMouseY(), x, y, 16, 16, scale)) {
                    context.drawItemTooltip(MC.textRenderer, stack, (int) event.getMouseX(), (int) event.getMouseY());
                }

                count++;
            }

            if (clickedX != -1 && clickedY != -1 && isHovered(clickedX, clickedY, startX, currentY, width, height, scale)) {
                INVENTORY_MANAGER.getClickHandler().pickupSlot(info.slot, true);
                clickedX = clickedY = -1;
            }

            currentY += height + MARGIN;
        }

        context.getMatrices().popMatrix();
        totalHeight = currentY - offset;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClick(MouseClickEvent event) {
       // CHAT_MANAGER.sendRaw("mouse click event called");
        if (event.button() == 0) {
            clickedX = event.mouseX();
            clickedY = event.mouseY();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onScroll(MouseScrollEvent event) {
      //  CHAT_MANAGER.sendRaw("mouse scroll event called");
        float maxOffset = Math.min(-totalHeight + MC.getWindow().getScaledHeight() / (scale.get()).floatValue(), 0);
        offset = (int) MathHelper.clamp(offset + (int) Math.ceil(event.amount()) * (scrollsensitivity.get() * 10), maxOffset, 0);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private int getShulkerColor(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return ColorHelper.getArgb(255, 128, 128, 128);

        if (!(blockItem.getBlock() instanceof ShulkerBoxBlock shulker)) return ColorHelper.getArgb(255, 128, 128, 128);

        DyeColor color = shulker.getColor();
        if (color == null) return ColorHelper.getArgb(255, 128, 0, 128);

        return DyeColorToARGB(color);
    }

    private int DyeColorToARGB(DyeColor color) {
        switch (color) {
            case WHITE: return ColorHelper.getArgb(255, 255, 255, 255);
            case ORANGE: return ColorHelper.getArgb(255, 216, 127, 51);
            case MAGENTA: return ColorHelper.getArgb(255, 178, 76, 216);
            case LIGHT_BLUE: return ColorHelper.getArgb(255, 102, 153, 216);
            case YELLOW: return ColorHelper.getArgb(255, 229, 229, 51);
            case LIME: return ColorHelper.getArgb(255, 127, 204, 25);
            case PINK: return ColorHelper.getArgb(255, 242, 127, 165);
            case GRAY: return ColorHelper.getArgb(255, 76, 76, 76);
            case LIGHT_GRAY: return ColorHelper.getArgb(255, 153, 153, 153);
            case CYAN: return ColorHelper.getArgb(255, 76, 127, 153);
            case PURPLE: return ColorHelper.getArgb(255, 127, 63, 178);
            case BLUE: return ColorHelper.getArgb(255, 51, 76, 178);
            case BROWN: return ColorHelper.getArgb(255, 102, 76, 51);
            case GREEN: return ColorHelper.getArgb(255, 102, 127, 51);
            case RED: return ColorHelper.getArgb(255, 153, 51, 51);
            case BLACK: return ColorHelper.getArgb(255, 25, 25, 25);
            default: return ColorHelper.getArgb(255, 128, 128, 128);
        }
    }

    private boolean isHovered(double mx, double my, int x, int y, int width, int height, float scale) {
        mx /= scale;
        my /= scale;
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    private static class ShulkerInfo {
        final ItemStack shulker;
        final int slot;
        final List<ItemStack> stacks;
        final int rows;
        final int cols;

        public ShulkerInfo(ItemStack shulker, int slot, List<ItemStack> stacks) {
            this.shulker = shulker;
            this.slot = slot;
            this.stacks = stacks;
            int size = stacks.size();
            this.rows = (int) Math.ceil(size / 9f);
            this.cols = Math.min(9, size);
        }

        public static ShulkerInfo create(ItemStack stack, int slot, boolean compact) {
            if (!(stack.getItem() instanceof BlockItem item)) return null;
            if (!(item.getBlock() instanceof net.minecraft.block.ShulkerBoxBlock)) return null;

            List<ItemStack> items = new ArrayList<>(Collections.nCopies(27, ItemStack.EMPTY));
            var component = stack.getComponents().getOrDefault(net.minecraft.component.DataComponentTypes.CONTAINER, null);
            if (component == null) return null;

            List<ItemStack> input = component.stream().toList();
            for (int i = 0; i < input.size(); i++) items.set(i, input.get(i));

            if (compact) items = compactList(items);

            return new ShulkerInfo(stack, slot, items);
        }

        private static List<ItemStack> compactList(List<ItemStack> input) {
            Map<Item, Integer> map = new HashMap<>();
            for (ItemStack stack : input) {
                if (stack.isEmpty()) continue;
                map.put(stack.getItem(), map.getOrDefault(stack.getItem(), 0) + stack.getCount());
            }

            List<ItemStack> result = new ArrayList<>();
            for (Map.Entry<Item, Integer> entry : map.entrySet()) {
                result.add(new ItemStack(entry.getKey(), entry.getValue()));
            }
            return result;
        }

        public int slot() {
            return slot;
        }

        public int rows() {
            return rows;
        }

        public int cols() {
            return cols;
        }
    }
}
