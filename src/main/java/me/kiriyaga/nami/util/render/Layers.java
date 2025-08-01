/*
 * Originally taken from https://github.com/mioclient/oyvey-ported since im lazy to write my own renderers
 * Please take a note that this code can be sublicensed by its owner
 */

package me.kiriyaga.nami.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.OptionalDouble;
import java.util.function.Function;

import static me.kiriyaga.nami.util.render.Pipelines.*;

public class Layers {
    private static final RenderLayer GLOBAL_QUADS;
    private static final Function<Double, RenderLayer> GLOBAL_LINES;
    private static final RenderLayer GLOBAL_TEXT;
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

    public static final Function<Identifier, RenderLayer> GLOBAL_ITEM = Util.memoize((Identifier identifier) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(identifier, false))
                .target(RenderPhase.Target.ITEM_ENTITY_TARGET)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .build(true);

        return RenderLayer.of(
                "item_entity_translucent_cull_nodepth",
                1536,
                true,
                true,
                CUSTOM_ITEM_ENTITY_PIPELINE,
                multiPhaseParameters);
    });

    public static final Function<Identifier, RenderLayer> GLOBAL_ITEM_WITH_GLINT = Util.memoize((Identifier identifier) -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(identifier, false))
                .target(RenderPhase.Target.ITEM_ENTITY_TARGET)
                .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                .texturing(RenderPhase.GLINT_TEXTURING)
                .build(true);

        return RenderLayer.of(
                "item_entity_translucent_cull_nodepth_with_glint",
                1536,
                true,
                true,
                CUSTOM_ITEM_ENTITY_PIPELINE,
                multiPhaseParameters);
    });

    static {
        GLOBAL_QUADS = RenderLayer.of("global_fill", 156, GLOBAL_QUADS_PIPELINE, empty());

        GLOBAL_TEXT = RenderLayer.of("global_text", 156, Pipelines.GLOBAL_TEXT_PIPELINE, empty());

        GLOBAL_LINES = Util.memoize(l -> {
            RenderPhase.LineWidth width = new RenderPhase.LineWidth(OptionalDouble.of(l));
            return RenderLayer.of("global_lines", 156, GLOBAL_LINES_PIPELINE, builder().lineWidth(width).build(false));
        });

        GLOBAL_GLINT = RenderLayer.of(
                "global_glint",
                1536,
                GLOBAL_GLINT_PIPELINE,
                RenderLayer.MultiPhaseParameters.builder()
                        .texture(new RenderPhase.Texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT, false))
                        .texturing(RenderPhase.GLINT_TEXTURING)
                        .build(false)
        );
    }
}