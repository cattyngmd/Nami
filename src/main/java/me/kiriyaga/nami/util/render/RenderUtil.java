/*
 * Originally taken from https://github.com/mioclient/ oyvey-ported since im lazy to write my own renderers
 * Please take a note that this code can be sublicensed by its owner
 */

package me.kiriyaga.nami.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import me.kiriyaga.nami.util.MatrixCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32C;

import java.lang.Math;

import static me.kiriyaga.nami.Nami.*;
import java.awt.*;

public class RenderUtil {

    public static void rect(DrawContext stack, float x1, float y1, float x2, float y2, int color) {
        rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void rect(DrawContext stack, float x1, float y1, float x2, float y2, int color, float width) {
        drawHorizontalLine(stack, x1, x2, y1, color, width);
        drawVerticalLine(stack, x2, y1, y2, color, width);
        drawHorizontalLine(stack, x1, x2, y2, color, width);
        drawVerticalLine(stack, x1, y1, y2, color, width);
    }

    protected static void drawHorizontalLine(DrawContext matrices, float x1, float x2, float y, int color) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + 1, y + 1, color);
    }

    protected static void drawVerticalLine(DrawContext matrices, float x, float y1, float y2, int color) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + 1, x + 1, y2, color);
    }

    protected static void drawHorizontalLine(DrawContext matrices, float x1, float x2, float y, int color, float width) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        rectFilled(matrices, x1, y, x2 + width, y + width, color);
    }

    private static void drawLineRect(DrawContext drawContext, Vec2f start, Vec2f end, int color, float width) {
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        int segments = (int) (length / width);

        float stepX = dx / segments;
        float stepY = dy / segments;

        for (int i = 0; i < segments; i++) {
            float x1 = start.x + stepX * i;
            float y1 = start.y + stepY * i;
            float x2 = x1 + width;
            float y2 = y1 + width;
            rectFilled(drawContext, x1, y1, x2, y2, color);
        }
    }

    protected static void drawVerticalLine(DrawContext matrices, float x, float y1, float y2, int color, float width) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        rectFilled(matrices, x, y1 + width, x + width, y2, color);
    }

    public static void rectFilled(DrawContext drawContext, float x1, float y1, float x2, float y2, int color) {
        int ix1 = (int) Math.min(x1, x2);
        int iy1 = (int) Math.min(y1, y2);
        int ix2 = (int) Math.max(x1, x2);
        int iy2 = (int) Math.max(y1, y2);

        drawContext.fill(ix1, iy1, ix2, iy2, color);
    }

    public static void rect3d(MatrixStack matrix, float x1, float y1, float x2, float y2, int color) {
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
        if (box.contains(MC.getEntityRenderDispatcher().camera.getPos())) return;

        Camera camera = MC.getEntityRenderDispatcher().camera;
        Vec3d camPos = camera.getPos();

        float minX = (float) (box.minX - camPos.x);
        float minY = (float) (box.minY - camPos.y);
        float minZ = (float) (box.minZ - camPos.z);
        float maxX = (float) (box.maxX - camPos.x);
        float maxY = (float) (box.maxY - camPos.y);
        float maxZ = (float) (box.maxZ - camPos.z);

        float epsilon = 1e-3f;

        boolean drawNorth = camPos.z < box.minZ - epsilon;
        boolean drawSouth = camPos.z > box.maxZ + epsilon;
        boolean drawWest  = camPos.x < box.minX - epsilon;
        boolean drawEast  = camPos.x > box.maxX + epsilon;
        boolean drawDown  = camPos.y < box.minY - epsilon;
        boolean drawUp    = camPos.y > box.maxY + epsilon;

        BufferBuilder buf = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = stack.peek().getPositionMatrix();

        if (drawDown) {
            buf.vertex(matrix, minX, minY, minZ).color(c.getRGB());
            buf.vertex(matrix, maxX, minY, minZ).color(c.getRGB());
            buf.vertex(matrix, maxX, minY, maxZ).color(c.getRGB());
            buf.vertex(matrix, minX, minY, maxZ).color(c.getRGB());
        }

        if (drawUp) {
            buf.vertex(matrix, minX, maxY, minZ).color(c.getRGB());
            buf.vertex(matrix, minX, maxY, maxZ).color(c.getRGB());
            buf.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB());
            buf.vertex(matrix, maxX, maxY, minZ).color(c.getRGB());
        }

        if (drawNorth) {
            buf.vertex(matrix, minX, minY, minZ).color(c.getRGB());
            buf.vertex(matrix, minX, maxY, minZ).color(c.getRGB());
            buf.vertex(matrix, maxX, maxY, minZ).color(c.getRGB());
            buf.vertex(matrix, maxX, minY, minZ).color(c.getRGB());
        }

        if (drawSouth) {
            buf.vertex(matrix, minX, minY, maxZ).color(c.getRGB());
            buf.vertex(matrix, maxX, minY, maxZ).color(c.getRGB());
            buf.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB());
            buf.vertex(matrix, minX, maxY, maxZ).color(c.getRGB());
        }

        if (drawWest) {
            buf.vertex(matrix, minX, minY, minZ).color(c.getRGB());
            buf.vertex(matrix, minX, minY, maxZ).color(c.getRGB());
            buf.vertex(matrix, minX, maxY, maxZ).color(c.getRGB());
            buf.vertex(matrix, minX, maxY, minZ).color(c.getRGB());
        }

        if (drawEast) {
            buf.vertex(matrix, maxX, minY, maxZ).color(c.getRGB());
            buf.vertex(matrix, maxX, minY, minZ).color(c.getRGB());
            buf.vertex(matrix, maxX, maxY, minZ).color(c.getRGB());
            buf.vertex(matrix, maxX, maxY, maxZ).color(c.getRGB());
        }

        Layers.getGlobalQuads().draw(buf.end());
    }

    public static void drawBoxFilled(MatrixStack stack, Vec3d vec, Color color) {
        drawBoxFilled(stack, Box.from(vec), color);
    }

    public static void drawBoxFilled(MatrixStack stack, BlockPos pos, Color color) {
        drawBoxFilled(stack, new Box(pos), color);
    }

    public static void drawBoxLines(MatrixStack stack, Box box, Color color, double lineWidth) {
        Camera camera = MC.getEntityRenderDispatcher().camera;
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
        double scaledLineWidth = Math.max(maxThickness / (1.0 + (distance / scaleFactor)), minThickness);

        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL);

        VertexRendering.drawBox(stack, buffer, minX, minY, minZ, maxX, maxY, maxZ,
                color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        Layers.getGlobalLines(scaledLineWidth).draw(buffer.end());
    }

    public static void drawBoxLines(MatrixStack stack, Vec3d vec, Color color, double lineWidth) {
        drawBoxLines(stack, Box.from(vec), color, lineWidth);
    }

    public static void drawBoxLines(MatrixStack stack, BlockPos pos, Color color, double lineWidth) {
        drawBoxLines(stack, new Box(pos), color, lineWidth);
    }


    public static void drawBox(MatrixStack stack, Box box, Color fillColor, Color lineColor,
                               double lineWidth, boolean filled, boolean outline) {
        if (filled) drawBoxFilled(stack, box, fillColor);
        if (outline) drawBoxLines(stack, box, lineColor, lineWidth);
    }

    public static void drawBox(MatrixStack stack, Vec3d vec, Color fillColor, Color lineColor,
                               double lineWidth, boolean filled, boolean outline) {
        drawBox(stack, Box.from(vec), fillColor, lineColor, lineWidth, filled, outline);
    }

    public static void drawBox(MatrixStack stack, BlockPos pos, Color fillColor, Color lineColor,
                               double lineWidth, boolean filled, boolean outline) {
        drawBox(stack, new Box(pos), fillColor, lineColor, lineWidth, filled, outline);
    }

    public static void drawBlockShape(MatrixStack matrices, World world, BlockPos pos, BlockState state,
                                      Color fillColor, Color lineColor, double lineWidth, boolean filled) {

        VoxelShape shape = state.getOutlineShape(world, pos);

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Box box = new Box(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );

            if (filled) drawBoxFilled(matrices, box, fillColor);
            drawBoxLines(matrices, box, lineColor, lineWidth);
        });
    }

    public static Color getBlockColor(BlockState state, BlockRenderView world, BlockPos pos) {
        BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
        int colorInt = blockColors.getColor(state, world, pos, 0);
        return new Color(colorInt, true);
    }

    public static void drawBoxPreset(MatrixStack stack, Box box, Color baseColor) {
        Color fill = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50);
        Color outline = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 90);
        drawBox(stack, box, fill, outline, 1.5, true, true);
    }

    public static void drawBoxPreset(MatrixStack stack, Vec3d vec, Color baseColor) {
        drawBoxPreset(stack, Box.from(vec), baseColor);
    }

    public static void drawBoxPreset(MatrixStack stack, BlockPos pos, Color baseColor) {
        drawBoxPreset(stack, new Box(pos), baseColor);
    }

    public static MatrixStack matrixFrom(Vec3d pos) {
        MatrixStack matrices = new MatrixStack();
        Camera camera = MC.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
        return matrices;
    }

    public static void drawText3D(MatrixStack matrices, Text text, Vec3d pos, float scale, boolean background, boolean border, float borderWidth) {
        Camera camera = MC.gameRenderer.getCamera();

        matrices.push();
        matrices.translate(
                pos.x - camera.getPos().x,
                pos.y - camera.getPos().y,
                pos.z - camera.getPos().z
        );

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

        matrices.scale(-scale, -scale, scale);

        TextRenderer textRenderer = FONT_MANAGER.rendererProvider.getRenderer();
        float textWidth = FONT_MANAGER.getWidth(text) / 2f;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate provider = MC.getBufferBuilders().getEntityVertexConsumers();

        if (background) {
            float bgPadding = 1f;
            float height = FONT_MANAGER.getHeight();

            float left = -textWidth - bgPadding;
            float right = textWidth + bgPadding;
            float top = -bgPadding;
            float bottom = height;

            int backgroundColor = 0x90000000;
            int borderColor = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor().getRGB();

            RenderUtil.rect3d(matrices, left, top, right, bottom, backgroundColor);

            if (border){
                RenderUtil.rect3d(matrices, left - borderWidth, top, left, bottom, borderColor);
                RenderUtil.rect3d(matrices, right, top, right + borderWidth, bottom, borderColor);
                RenderUtil.rect3d(matrices, left - borderWidth, top - borderWidth, right + borderWidth, top, borderColor);
                RenderUtil.rect3d(matrices, left - borderWidth, bottom, right + borderWidth, bottom + borderWidth, borderColor);
            }
        }

        textRenderer.draw(
                text, -textWidth, 0, -1, !MODULE_MANAGER.getStorage().getByClass(FontModule.class).isEnabled(), matrix, provider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880
        );

        provider.draw();

        matrices.pop();
    }

    public static void renderItem3D(ItemStack stack, MatrixStack matrices, Vec3d pos, float scale, Vec3d lookDir) {
        ItemRenderer itemRenderer = MC.getItemRenderer();
        Camera camera = MC.gameRenderer.getCamera();

        matrices.push();

        Vec3d camPos = camera.getPos();

        matrices.translate((float)(pos.x - camPos.x), (float)(pos.y - camPos.y), (float)(pos.z - camPos.z));

        Vec3d dir = lookDir.normalize();

        Vec3d up = new Vec3d(0, 1, 0);
        Vec3d right = up.crossProduct(dir).normalize();
        if (right.lengthSquared() < 1e-6) {
            right = new Vec3d(1, 0, 0);
        }
        Vec3d newUp = dir.crossProduct(right).normalize();

        Matrix3f basis = new Matrix3f(
                (float) right.x, (float) right.y, (float) right.z,
                (float) newUp.x, (float) newUp.y, (float) newUp.z,
                (float) dir.x, (float) dir.y, (float) dir.z
        );

        Quaternionf rotation = new Quaternionf().setFromNormalized(basis);
        matrices.multiply(rotation);

        float s = scale * 13f;
        matrices.scale(s, s, s);
        matrices.scale(1.0f, 1.0f, 0.0001f);

        itemRenderer.renderItem(
                stack,
                ItemDisplayContext.FIXED,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV,
                matrices,
                MC.getBufferBuilders().getEntityVertexConsumers(),
                MC.world,
                0
        );

        MC.getBufferBuilders().getEntityVertexConsumers().draw();

        matrices.pop();
    }
}