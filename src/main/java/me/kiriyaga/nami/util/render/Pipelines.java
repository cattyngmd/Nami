/*
 * Originally taken from https://github.com/mioclient/oyvey-ported since im lazy to write my own renderers
 * Please take a note that this code can be sublicensed by its owner
 */

package me.kiriyaga.nami.util.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import static net.minecraft.client.gl.RenderPipelines.*;

class Pipelines {
    static final RenderPipeline GLOBAL_GLINT_PIPELINE = RenderPipeline.builder(new RenderPipeline.Snippet[]{
                    TRANSFORMS_AND_PROJECTION_SNIPPET,
                    FOG_SNIPPET,
                    GLOBALS_SNIPPET
            })
            .withLocation("pipeline/global_glint_pipeline")
            .withVertexShader("core/glint")
            .withFragmentShader("core/glint")
            .withSampler("Sampler0")
            .withDepthWrite(false)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.GLINT)
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .build();

    static final RenderPipeline GLOBAL_QUADS_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation("pipeline/global_fill_pipeline")
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    static final RenderPipeline GLOBAL_LINES_PIPELINE = RenderPipeline.builder(RENDERTYPE_LINES_SNIPPET)
            .withLocation("pipeline/global_lines_pipeline")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    static final RenderPipeline GLOBAL_TEXT_PIPELINE = RenderPipeline.builder(POSITION_COLOR_SNIPPET)
            .withLocation("pipeline/global_text_pipeline")
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    static final RenderPipeline CUSTOM_ITEM_ENTITY_PIPELINE = RenderPipeline.builder(POSITION_TEX_COLOR_SNIPPET)
            .withLocation("pipeline/custom_item_entity_pipeline")
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .build();
}