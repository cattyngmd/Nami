package namidevelopment.kiriyaga.nami.api.font;

import namidevelopment.kiriyaga.nami.impl.feature.impl.client.FontFeature;
import namidevelopment.kiriyaga.nami.util.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class FontService {

    public final FontLoader fontLoader = new FontLoader();
    public final FontRendererProvider rendererProvider = new FontRendererProvider(fontLoader);
    public final FontMetrics fontMetrics = new FontMetrics(rendererProvider);

    public void init() {
        fontLoader.init();
    }

    public void drawText(GuiGraphics context, Component text, int x, int y, boolean shadow, int color) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(0.5f, 0.5f);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), FEATURE_SERVICE.getStorage().getByClass(FontFeature.class).shadowDarken.get()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, false);
    }

    public void drawText(GuiGraphics context, String text, int x, int y, boolean shadow, int color) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(0.5f, 0.5f);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), FEATURE_SERVICE.getStorage().getByClass(FontFeature.class).shadowDarken.get()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, false);
    }

    public void drawText(GuiGraphics context, Component text, int x, int y, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(0.5f, 0.5f);
            context.drawString(rendererProvider.getRenderer(), ColorUtils.darken(text, FEATURE_SERVICE.getStorage().getByClass(FontFeature.class).shadowDarken.get()), x, y, 0xFFFFFFFF, false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, 0xFFFFFFFF, false);
    }

    public void drawText(GuiGraphics context, String text, int x, int y, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(0.5f, 0.5f);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(0xFFFFFFFF), FEATURE_SERVICE.getStorage().getByClass(FontFeature.class).shadowDarken.get()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, 0xFFFFFFFF, false);
    }

    public void drawText(GuiGraphics context, Component text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(0.5f, 0.5f);
            context.drawString(rendererProvider.getRenderer(), ColorUtils.darken(text, FEATURE_SERVICE.getStorage().getByClass(FontFeature.class).shadowDarken.get()), x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), 100).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, false);
    }

    public void drawText(GuiGraphics context, String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(0.5f, 0.5f);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), FEATURE_SERVICE.getStorage().getByClass(FontFeature.class).shadowDarken.get()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, false);
    }

    public int getWidth(Component text) {
        return rendererProvider.getRenderer().width(text);
    }

    public int getWidth(String text) {
        return rendererProvider.getRenderer().width(text);
    }

    public int getHeight() {
        return fontMetrics.getHeight();
    }
}
