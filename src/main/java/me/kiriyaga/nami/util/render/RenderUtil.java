/*
 * Originally taken from https://github.com/mioclient/ oyvey-ported since im lazy to write my own renderers
 * Please take a note that this code can be sublicensed by its owner
 */

package me.kiriyaga.nami.util.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import net.minecraft.gizmos.Gizmos;
import org.joml.*;
import net.minecraft.core.BlockPos;

import static me.kiriyaga.nami.Nami.*;
import java.awt.*;

public class RenderUtil {

    public static void rect3d(PoseStack matrix, float x1, float y1, float x2, float y2, int color) {
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

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y2, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(matrix.last().pose(), x2, y2, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(matrix.last().pose(), x2, y1, 0.0F).setColor(g, h, j, f);
        bufferBuilder.addVertex(matrix.last().pose(), x1, y1, 0.0F).setColor(g, h, j, f);

        Layers.getGlobalQuads().draw(bufferBuilder.buildOrThrow());
    }


    // 3d
    //   net.minecraft.client.render.debug.ChunkBorderDebugRenderer
    public static void drawBlockPosLines(Level world, BlockPos pos, BlockState state, Color color, boolean filled, boolean outlined, float outlineWidth) {
        VoxelShape shape = state.getShape(world, pos);

        int fillColor = ARGB.color(
                50,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        int outlineColor = ARGB.color(
                90,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            AABB box = new AABB(
                    pos.getX() + minX,
                    pos.getY() + minY,
                    pos.getZ() + minZ,
                    pos.getX() + maxX,
                    pos.getY() + maxY,
                    pos.getZ() + maxZ
            );

            if (filled) {
                Gizmos.cuboid(
                        box,
                        GizmoStyle.fill(fillColor)
                ).setAlwaysOnTop();
            }

            if (outlined) {
                Gizmos.cuboid(
                        box,
                        GizmoStyle.stroke(outlineColor, outlineWidth)
                ).setAlwaysOnTop();
            }
        });
    }

    public static void drawBoxLines(AABB box, Color color, boolean filled, boolean outlined, float outlineWidth) {
        int fillColor = ARGB.color(
                50,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        int outlineColor = ARGB.color(
                90,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        if (filled) {
            Gizmos.cuboid(
                    box,
                    GizmoStyle.fill(fillColor)
            ).setAlwaysOnTop();
        }

        if (outlined) {
            Gizmos.cuboid(
                    box,
                    GizmoStyle.stroke(outlineColor, outlineWidth)
            ).setAlwaysOnTop();
        }
    }

    public static void drawLine(Vec3 from, Vec3 to, Color color, float width) {
        int lineColor = ARGB.color(
                255,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );

        Gizmos.line(from, to, lineColor, width)
                .setAlwaysOnTop();
    }

    public static void drawText3D(PoseStack matrices, Component text, Vec3 pos, float scale, boolean background, boolean border, float borderWidth) {
        Camera camera = MC.gameRenderer.getMainCamera();

        matrices.pushPose();
        matrices.translate(
                pos.x - camera.position().x,
                pos.y - camera.position().y,
                pos.z - camera.position().z
        );

        matrices.mulPose(Axis.YP.rotationDegrees(-camera.yRot()));
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));

        matrices.scale(-scale, -scale, scale);

        Font textRenderer = FONT_MANAGER.rendererProvider.getRenderer();
        float textWidth = FONT_MANAGER.getWidth(text) / 2f;

        Matrix4f matrix = matrices.last().pose();
        MultiBufferSource.BufferSource provider = MC.renderBuffers().bufferSource();

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

        textRenderer.drawInBatch(
                text, -textWidth, 0, -1, !MODULE_MANAGER.getStorage().getByClass(FontModule.class).isEnabled(), matrix, provider, Font.DisplayMode.SEE_THROUGH, 0, 15728880
        );

        provider.endBatch();

        matrices.popPose();
    }

    public static void renderItem3D(ItemStack stack, PoseStack matrices, Vec3 pos, float scale, Vec3 lookDir) {
        ItemRenderer itemRenderer = MC.getItemRenderer();
        Camera camera = MC.gameRenderer.getMainCamera();

        matrices.pushPose();

        Vec3 camPos = camera.position();

        matrices.translate((float)(pos.x - camPos.x), (float)(pos.y - camPos.y), (float)(pos.z - camPos.z));

        Vec3 dir = lookDir.normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = up.cross(dir).normalize();
        if (right.lengthSqr() < 1e-6) {
            right = new Vec3(1, 0, 0);
        }
        Vec3 newUp = dir.cross(right).normalize();

        Matrix3f basis = new Matrix3f(
                (float) right.x, (float) right.y, (float) right.z,
                (float) newUp.x, (float) newUp.y, (float) newUp.z,
                (float) dir.x, (float) dir.y, (float) dir.z
        );

        Quaternionf rotation = new Quaternionf().setFromNormalized(basis);
        matrices.mulPose(rotation);

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

        MC.renderBuffers().bufferSource().endBatch();

        matrices.popPose();
    }
}