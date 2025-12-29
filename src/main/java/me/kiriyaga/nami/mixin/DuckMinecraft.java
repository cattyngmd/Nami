package me.kiriyaga.nami.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface DuckMinecraft {
    @Accessor("font")
    void setFont(Font renderer);
}