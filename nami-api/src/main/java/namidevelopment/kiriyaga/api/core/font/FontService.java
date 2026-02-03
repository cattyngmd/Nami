package namidevelopment.kiriyaga.api.core.font;

import static namidevelopment.kiriyaga.api.NamiApi.*;

import namidevelopment.kiriyaga.api.contract.FeatureContractService;
import namidevelopment.kiriyaga.api.contract.feature.FontFeatureConfig;
import namidevelopment.kiriyaga.api.util.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FontService {

    public final FontLoader fontLoader = new FontLoader();
    public final FontRendererProvider rendererProvider = new FontRendererProvider(fontLoader);
    public final FontMetrics fontMetrics = new FontMetrics(rendererProvider);

    private float translate = 0.50f;
    
    public void init() {
        fontLoader.init();
    }

    private FontFeatureConfig font() {
        return FeatureContractService.get(FontFeatureConfig.class);
    }
    
    public void drawText(GuiGraphics context, Component text, int x, int y, boolean shadow, int color) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(translate, translate);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), font().getShadowDarken()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, rendererProvider.getRenderer().equals(API_MC.font));
    }

    public void drawText(GuiGraphics context, String text, int x, int y, boolean shadow, int color) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(translate, translate);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), font().getShadowDarken()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, rendererProvider.getRenderer().equals(API_MC.font));
    }

    public void drawText(GuiGraphics context, Component text, int x, int y, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(translate, translate);
            context.drawString(rendererProvider.getRenderer(), ColorUtils.darken(text, font().getShadowDarken()), x, y, 0xFFFFFFFF, false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, 0xFFFFFFFF, rendererProvider.getRenderer().equals(API_MC.font));
    }

    public void drawText(GuiGraphics context, String text, int x, int y, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(translate, translate);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(0xFFFFFFFF), font().getShadowDarken()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, 0xFFFFFFFF, rendererProvider.getRenderer().equals(API_MC.font));
    }

    public void drawText(GuiGraphics context, Component text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(translate, translate);
            context.drawString(rendererProvider.getRenderer(), ColorUtils.darken(text, font().getShadowDarken()), x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), 100).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, rendererProvider.getRenderer().equals(API_MC.font));
    }

    public void drawText(GuiGraphics context, String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            var matrices = context.pose();
            matrices.pushMatrix();
            matrices.translate(translate, translate);
            context.drawString(rendererProvider.getRenderer(), text, x, y, ColorUtils.darken(ColorUtils.fromRGBA(color), font().getShadowDarken()).getRGB(), false);
            matrices.popMatrix();
        }

        context.drawString(rendererProvider.getRenderer(), text, x, y, color, rendererProvider.getRenderer().equals(API_MC.font));
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
