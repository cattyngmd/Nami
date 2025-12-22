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
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
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
    //   net.minecraft.client.render.debug.ChunkBorderDebugRenderer
    public static void drawBlockPosLines(World world, BlockPos pos, BlockState state, Color color, boolean filled, boolean outlined, float outlineWidth) {
        VoxelShape shape = state.getOutlineShape(world, pos);

        int fillColor = ColorHelper.getArgb(
                50,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        int outlineColor = ColorHelper.getArgb(
                90,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

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
                GizmoDrawing.box(
                        box,
                        DrawStyle.filled(fillColor)
                ).ignoreOcclusion();
            }

            if (outlined) {
                GizmoDrawing.box(
                        box,
                        DrawStyle.stroked(outlineColor, outlineWidth)
                ).ignoreOcclusion();
            }
        });
    }

    public static void drawBoxLines(Box box, Color color, boolean filled, boolean outlined, float outlineWidth) {
        int fillColor = ColorHelper.getArgb(
                50,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        int outlineColor = ColorHelper.getArgb(
                90,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        if (filled) {
            GizmoDrawing.box(
                    box,
                    DrawStyle.filled(fillColor)
            ).ignoreOcclusion();
        }

        if (outlined) {
            GizmoDrawing.box(
                    box,
                    DrawStyle.stroked(outlineColor, outlineWidth)
            ).ignoreOcclusion();
        }
    }

    public static void drawText3D(MatrixStack matrices, Text text, Vec3d pos, float scale, boolean background, boolean border, float borderWidth) {
        Camera camera = MC.gameRenderer.getCamera();

        matrices.push();
        matrices.translate(
                pos.x - camera.getCameraPos().x,
                pos.y - camera.getCameraPos().y,
                pos.z - camera.getCameraPos().z
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

        Vec3d camPos = camera.getCameraPos();

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

/*        itemRenderer.renderItem( // todo: fix this
                stack,
                ItemDisplayContext.FIXED,
                LightmapTextureManager.MAX_LIGHT_COORDINATE,
                OverlayTexture.DEFAULT_UV,
                matrices,
                MC.getBufferBuilders().getEntityVertexConsumers(),
                MC.world,
                0
        );*/

        MC.getBufferBuilders().getEntityVertexConsumers().draw();

        matrices.pop();
    }
}