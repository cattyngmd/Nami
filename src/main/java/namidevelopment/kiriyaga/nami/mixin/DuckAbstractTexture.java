package namidevelopment.kiriyaga.nami.mixin;

import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractTexture.class)
public interface DuckAbstractTexture {

    @Accessor("sampler")
    void setSampler(GpuSampler sampler);
}
