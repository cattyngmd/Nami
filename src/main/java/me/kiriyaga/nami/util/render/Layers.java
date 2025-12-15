package me.kiriyaga.nami.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.Util;
import java.util.function.Function;

public class Layers {
    private static final RenderLayer GLOBAL_QUADS;
    private static final RenderLayer GLOBAL_TEXT;
    private static final Function<Double, RenderLayer> GLOBAL_LINES;

    public static RenderLayer getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    public static RenderLayer getGlobalText() {
        return GLOBAL_TEXT;
    }

    public static RenderLayer getGlobalLines(double width) {
        return GLOBAL_LINES.apply(width);
    }

    static {
        GLOBAL_QUADS = RenderLayers.solid();

        GLOBAL_TEXT = RenderLayers.textBackground();

        GLOBAL_LINES = Util.memoize(width -> {
            return RenderLayers.lines();
        });
    }
}
