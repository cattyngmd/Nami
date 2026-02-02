package namidevelopment.kiriyaga.nami.mixin;

import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Font.class)
public interface DuckFont {
    @Accessor("provider")
    Font.Provider getProvider();
}
