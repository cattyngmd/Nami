package namidevelopment.kiriyaga.nami.util.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

/**
 * Simple {@link GuiElementRenderState} with per-vertex colors
 */
public record RectangleRenderState(Matrix3x2fc pose, int x0, int y0, int x1, int y1, int col1, int col2, int col3, int col4, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    public RectangleRenderState(Matrix3x2fc pose, int x0, int y0, int x1, int y1, int col1, int col2, int col3, int col4, @Nullable ScreenRectangle screenRectangle) {
        this(pose, x0, y0, x1, y1, col1, col2, col3, col4, screenRectangle, getBounds(x0, y0, x1, y1, pose, screenRectangle));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x0(), (float)this.y0()).setColor(this.col1());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x0(), (float)this.y1()).setColor(this.col2());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x1(), (float)this.y1()).setColor(this.col3());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x1(), (float)this.y0()).setColor(this.col4());
    }

    @Override
    public @NotNull RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public @NotNull TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    private static @Nullable ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2fc matrix3x2fc, @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = (new ScreenRectangle(i, j, k - i, l - j)).transformMaxBounds(matrix3x2fc);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}
