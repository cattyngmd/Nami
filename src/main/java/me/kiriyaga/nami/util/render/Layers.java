package me.kiriyaga.nami.util.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Util;
import java.util.function.Function;

public class Layers {
    private static final RenderType GLOBAL_QUADS;
    private static final RenderType GLOBAL_TEXT;
    private static final Function<Double, RenderType> GLOBAL_LINES;

    public static RenderType getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    public static RenderType getGlobalText() {
        return GLOBAL_TEXT;
    }

    public static RenderType getGlobalLines(double width) {
        return GLOBAL_LINES.apply(width);
    }

    static {
        GLOBAL_QUADS = RenderTypes.solidMovingBlock();

        GLOBAL_TEXT = RenderTypes.textBackground();

        GLOBAL_LINES = Util.memoize(width -> {
            return RenderTypes.lines();
        });
    }
}
