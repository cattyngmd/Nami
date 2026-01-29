package namidevelopment.kiriyaga.nami.util.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class Layers {
    private static final RenderType GLOBAL_QUADS;

    public static RenderType getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    static {
        GLOBAL_QUADS = RenderTypes.solidMovingBlock();
    }
}
