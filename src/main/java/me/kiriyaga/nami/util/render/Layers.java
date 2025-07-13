/*
 * Originally taken from https://github.com/mioclient/oyvey-ported since im lazy to write my own renderers
 * Please take a note that this code can be sublicensed by its owner
 */

package me.kiriyaga.nami.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Util;

import java.util.OptionalDouble;
import java.util.function.Function;

import static me.kiriyaga.nami.util.render.Pipelines.*;

public class Layers {
    private static final RenderLayer GLOBAL_QUADS;
    private static final Function<Double, RenderLayer> GLOBAL_LINES;
    private static final RenderLayer GLOBAL_TEXT;
    private static final RenderLayer GLOBAL_ITEM;
    private static final RenderLayer GLOBAL_GLINT;

    public static RenderLayer getGlobalGlint() {return GLOBAL_GLINT;}

    public static RenderLayer getGlobalLines(double width) {
        return GLOBAL_LINES.apply(width);
    }

    public static RenderLayer getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    public static RenderLayer getGlobalText() {
        return GLOBAL_TEXT;
    }

    public static RenderLayer getGlobalItem() {
        return GLOBAL_ITEM;
    }


    private static RenderLayer.MultiPhaseParameters.Builder builder() {
        return RenderLayer.MultiPhaseParameters.builder();
    }

    private static RenderLayer.MultiPhaseParameters empty() {
        return builder().build(false);
    }

    private static RenderLayer.MultiPhaseParameters withTexture() {
        return builder()
                .texture(new RenderPhase.Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false))
                .build(false);
    }

    static {
        GLOBAL_QUADS = RenderLayer.of("global_fill", 156, GLOBAL_QUADS_PIPELINE, empty());

        GLOBAL_TEXT = RenderLayer.of("global_text", 156, Pipelines.GLOBAL_TEXT_PIPELINE, empty());
        GLOBAL_ITEM = RenderLayer.of("global_item", 156, GLOBAL_ITEM_PIPELINE, withTexture());

        GLOBAL_LINES = Util.memoize(l -> {
            RenderPhase.LineWidth width = new RenderPhase.LineWidth(OptionalDouble.of(l));
            return RenderLayer.of("global_lines", 156, GLOBAL_LINES_PIPELINE, builder().lineWidth(width).build(false));
        });

        GLOBAL_GLINT = RenderLayer.of("global_glint", 156, Pipelines.GLOBAL_GLINT_PIPELINE,
                RenderLayer.MultiPhaseParameters.builder()
                        .texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, false))
                        .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
                        .build(false));
    }
}