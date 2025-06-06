package me.kiriyaga.essentials.mixin;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatHudLine.class)
public interface ChatHudLineAccessor {
    @Accessor("signature")
    @Nullable
    MessageSignatureData getSignature();
}
