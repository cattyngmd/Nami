/*
 * Originally taken from https://github.com/mioclient/ oyvey-ported since im lazy to write my own renderers
 * Please take a note that this code can be sublicensed by its owner
 */

package me.kiriyaga.nami.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.*;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.lang.Math;

import static me.kiriyaga.nami.Nami.*;
import java.awt.*;

public class RenderUtil {

    public static void rect(MatrixStack stack, float x1, float y1, float x2, float y2, int color) {
        rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void rect(MatrixStack stack, float x1, float y1, float x2, float y2, int color, float width) {
        drawHorizontalLine(stack, x1, x2, y1, color, width);
        drawVerticalLine(stack, x2, y1, y2, color, width);
        drawHorizontalLine(stack, x1, x2, y2, color, width);
        drawVerticalLine(stack, x1, y1, y2, color, width);
    }

    protected static void drawHorizontalLine(MatrixStack matrices, float x1, float x2, float y, int color) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + 1, y + 1, color);
    }

    protected static void drawVerticalLine(MatrixStack matrices, float x, float y1, float y2, int color) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + 1, x + 1, y2, color);
    }

    protected static void drawHorizontalLine(MatrixStack matrices, float x1, float x2, float y, int color, float width) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + width, y + width, color);
    }

    protected static void drawVerticalLine(MatrixStack matrices, float x, float y1, float y2, int color, float width) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + width, x + width, y2, color);
    }

    public static void rectFilled(MatrixStack matrix, float x1, float y1, float x2, float y2, int color) {
        float i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float j = (float) (color & 255) / 255.0F;

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x1, y2, 0.0F).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x2, y2, 0.0F).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x2, y1, 0.0F).color(g, h, j, f);
        bufferBuilder.vertex(matrix.peek().getPositionMatrix(), x1, y1, 0.0F).color(g, h, j, f);

        Layers.getGlobalQuads().draw(bufferBuilder.end());
    }

    // 3d
    public static void drawBoxFilled(MatrixStack stack, Box box, Color c) {
        float minX = (float) (box.minX - MINECRAFT.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - MINECRAFT.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - MINECRAFT.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - MINECRAFT.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - MINECRAFT.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - MINECRAFT.getEntityRenderDispatcher().camera.getPos().getZ());

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(stack.peek().getPositionMatrix(), minX, maxY, minZ).color(c.getRGB());

        Layers.getGlobalQuads().draw(bufferBuilder.end());
    }

    public static void drawBoxFilled(MatrixStack stack, Vec3d vec, Color c) {
        drawBoxFilled(stack, Box.from(vec), c);
    }

    public static void drawBoxFilled(MatrixStack stack, BlockPos bp, Color c) {
        drawBoxFilled(stack, new Box(bp), c);
    }

    public static void drawBox(MatrixStack stack, Box box, Color fillColor, Color lineColor, double lineWidth, boolean filled, boolean outline) {
        if (filled) {
            drawBoxFilled(stack, box, fillColor);
        }
        if (outline) {
            drawBox(stack, box, lineColor, lineWidth);
        }
    }

    public static void drawBlockShape(MatrixStack matrices, World world, BlockPos pos, BlockState state,
                                      Color fillColor, Color lineColor, double lineWidth, boolean filled) {

        VoxelShape shape = state.getOutlineShape(world, pos);

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Box box = new Box(
                    pos.getX() + minX,
                    pos.getY() + minY,
                    pos.getZ() + minZ,
                    pos.getX() + maxX,
                    pos.getY() + maxY,
                    pos.getZ() + maxZ
            );

            if (filled) {
                drawBoxFilled(matrices, box, fillColor);
            }
            drawBox(matrices, box, lineColor, lineWidth);
        });
    }

    public static Color getBlockColor(BlockState state, BlockRenderView world, BlockPos pos) {
        BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
        int colorInt = blockColors.getColor(state, world, pos, 0);

        return new Color(colorInt, true);
    }


    public static void drawBox(MatrixStack stack, Box box, Color c, double lineWidth) {
        Camera camera = MINECRAFT.getEntityRenderDispatcher().camera;

        float minX = (float) (box.minX - camera.getPos().getX());
        float minY = (float) (box.minY - camera.getPos().getY());
        float minZ = (float) (box.minZ - camera.getPos().getZ());
        float maxX = (float) (box.maxX - camera.getPos().getX());
        float maxY = (float) (box.maxY - camera.getPos().getY());
        float maxZ = (float) (box.maxZ - camera.getPos().getZ());

        Vec3d center = box.getCenter();
        double distance = camera.getPos().distanceTo(center);

        double minThickness = 0.5;
        double maxThickness = lineWidth;
        double scaleFactor = 5.0;

        double scaledLineWidth = maxThickness / (1.0 + (distance / scaleFactor));
        scaledLineWidth = Math.max(scaledLineWidth, minThickness);

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL);

        VertexRendering.drawBox(stack, bufferBuilder, minX, minY, minZ, maxX, maxY, maxZ,
                c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);

        Layers.getGlobalLines(scaledLineWidth).draw(bufferBuilder.end());
    }



    public static void drawBox(MatrixStack stack, Vec3d vec, Color c, double lineWidth) {
        drawBox(stack, Box.from(vec), c, lineWidth);
    }

    public static void drawBox(MatrixStack stack, BlockPos bp, Color c, double lineWidth) {
        drawBox(stack, new Box(bp), c, lineWidth);
    }

    public static MatrixStack matrixFrom(Vec3d pos) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = MINECRAFT.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
        return matrices;
    }

    public static void drawText2D(
            DrawContext drawContext,
            Text text,
            int x,
            int y,
            int color,
            boolean withBackground
    ) {
        if (withBackground) {
            int padding = 2;
            int textWidth = MINECRAFT.textRenderer.getWidth(text);
            int textHeight = MINECRAFT.textRenderer.fontHeight;
            int bgColor = (180 << 24) | 0x000000;
            drawContext.fill(x - padding, y - padding, x + textWidth + padding, y + textHeight + padding, bgColor);
        }
        drawContext.drawText(MINECRAFT.textRenderer, text, x, y, color, false);
    }


    public static void drawItem2D(
            DrawContext drawContext,
            ItemStack stack,
            int x,
            int y,
            float scale
    ) {
        Matrix3x2fStack matrices = drawContext.getMatrices();
        matrices.pushMatrix();

        matrices.translate(x, y);
        matrices.scale(scale, scale);

        drawContext.drawItem(stack, -8, -8);

        matrices.popMatrix();
    }

    public static void drawText3D(MatrixStack matrices, Text text, Vec3d pos, float scale, boolean background, boolean border, float borderWidth) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        matrices.push();
        matrices.translate(
                pos.x - camera.getPos().x,
                pos.y - camera.getPos().y,
                pos.z - camera.getPos().z
        );

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

        matrices.scale(-scale, -scale, scale);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float textWidth = textRenderer.getWidth(text) / 2f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate provider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        if (background) {
            float bgPadding = 1f;
            float height = textRenderer.fontHeight;

            float left = -textWidth - bgPadding;
            float right = textWidth + bgPadding;
            float top = -bgPadding;
            float bottom = height + bgPadding;

            int backgroundColor = 0x90000000;
            int borderColor = MODULE_MANAGER.getModule(ColorModule.class).getStyledGlobalColor().getRGB();

            RenderUtil.rectFilled(matrices, left, top, right, bottom, backgroundColor);

            if (border){
                RenderUtil.rectFilled(matrices, left - borderWidth, top, left, bottom, borderColor);
                RenderUtil.rectFilled(matrices, right, top, right + borderWidth, bottom, borderColor);
                RenderUtil.rectFilled(matrices, left - borderWidth, top - borderWidth, right + borderWidth, top, borderColor);
                RenderUtil.rectFilled(matrices, left - borderWidth, bottom, right + borderWidth, bottom + borderWidth, borderColor);
            }
        }

        textRenderer.draw(
                text, -textWidth, 0, -1, true, matrix, provider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880
        );

        provider.draw();

        matrices.pop();
    }

    public static void renderItem3D(ItemStack stack, MatrixStack matrices, Vec3d pos, RenderLayer customLayer, float scale) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();

        matrices.push();

        Vec3d camPos = client.gameRenderer.getCamera().getPos();
        matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-client.gameRenderer.getCamera().getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(client.gameRenderer.getCamera().getPitch()));

        float s = scale * 13f;
        matrices.scale(s, s, s);

        VertexConsumerProvider.Immediate base = client.getBufferBuilders().getEntityVertexConsumers();

        VertexConsumerProvider redirectingProvider = requestedLayer -> {
            String name = requestedLayer.getName().toLowerCase();
            if (name.contains("item") || name.contains("cutout") || name.contains("entity")) {
                return base.getBuffer(customLayer);
            }
            return base.getBuffer(requestedLayer);
        };

        itemRenderer.renderItem(
                stack,
                ItemDisplayContext.GUI,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV,
                matrices,
                redirectingProvider,
                client.world,
                0
        );

        // since we are using our own pipeline and layer, we need to shade ourselfs, but i dont wanna so we just use second layer for glint
        if (stack.hasGlint()) {
            VertexConsumerProvider glintProvider = requestedLayer -> {
                String name = requestedLayer.getName().toLowerCase();
                if (name.contains("glint")) {
                    return base.getBuffer(customLayer);
                }
                return base.getBuffer(requestedLayer);
            };

            itemRenderer.renderItem(
                    stack,
                    ItemDisplayContext.GUI,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    glintProvider,
                    client.world,
                    0
            );
        }

        base.draw();

        matrices.pop();
    }
}