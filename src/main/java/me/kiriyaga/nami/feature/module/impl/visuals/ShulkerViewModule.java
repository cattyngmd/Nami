/*
 Originally made by @cattyngmd
 MIT (2024)
*/
package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.MouseClickEvent;
import me.kiriyaga.nami.event.impl.MouseScrollEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.setting.impl.ColorSetting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class ShulkerViewModule extends Module {

    public final BoolSetting compact = addSetting(new BoolSetting("compact", true));
    public final BoolSetting bothSides = addSetting(new BoolSetting("both sides", true));
    public final IntSetting scale = addSetting(new IntSetting("scale", 10, 1, 20));

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
        super("shulker view", "Improves shulker managment. Originally made by @cattyngmd.", ModuleCategory.of("visuals"),"shulkerview", "ыргдлукмшуц");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (!(MC.currentScreen instanceof HandledScreen<?> screen)) return;

        shulkerList.clear();
        for (Slot slot : screen.getScreenHandler().slots) {
            ShulkerInfo info = ShulkerInfo.create(slot.getStack(), slot.id, compact.get());
            if (info != null) shulkerList.add(info);
        }
    }

    @SubscribeEvent
    public void onRender(Render2DEvent event) {
        if (!(MC.currentScreen instanceof HandledScreen)) return;

        DrawContext context = event.getDrawContext();
        boolean right = false;
        currentY = bothSides.get() ? MARGIN : MARGIN + offset;
        startX = MARGIN;
        float scale = this.scale.get() / 10f;

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        for (ShulkerInfo info : shulkerList) {
            int rows = info.rows();
            int cols = info.cols();

            if (currentY >= MC.getWindow().getScaledHeight() / scale && bothSides.get() && !right) {
                right = true;
                currentY = MARGIN + offset;
            }

            if (right) {
                float totalWidth = cols * GRID_WIDTH + MARGIN * cols;
                startX = (int) ((MC.getWindow().getScaledWidth() - totalWidth - MARGIN) / scale);
            }

            int width = cols * GRID_WIDTH + MARGIN * cols;
            int height = rows * GRID_HEIGHT + MARGIN * rows;

            context.fill(startX, currentY, startX + width, currentY + height, new Color(0,0,0,75).getRGB());

            int count = 0;
            for (ItemStack stack : info.stacks) {
                if (compact.get() && stack.isEmpty()) break;
                int x = startX + (count % 9) * GRID_WIDTH + MARGIN;
                int y = currentY + (count / 9) * GRID_HEIGHT + MARGIN;

                context.drawItem(stack, x, y);
                context.drawStackOverlay(MC.textRenderer, stack, x, y, null);

                count++;
            }


            if (clickedX != -1 && clickedY != -1 && isHovered(clickedX, clickedY, startX, currentY, width, height, scale)) {
                MC.interactionManager.clickSlot(MC.player.currentScreenHandler.syncId, info.slot, 0, SlotActionType.PICKUP, MC.player);
                clickedX = clickedY = -1;
            }

            currentY += height + MARGIN;
        }

        context.getMatrices().popMatrix();
        totalHeight = currentY - offset;
    }

    @SubscribeEvent
    public void onClick(MouseClickEvent event) {
        if (event.button() == 0) {
            clickedX = event.mouseX();
            clickedY = event.mouseY();
        }
    }

    @SubscribeEvent
    public void onScroll(MouseScrollEvent event) {
        float maxOffset = Math.min(-totalHeight + MC.getWindow().getScaledHeight() / (scale.get() / 10f), 0);
        offset = (int) MathHelper.clamp(offset + (int) Math.ceil(event.amount()) * 10, maxOffset, 0);
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
