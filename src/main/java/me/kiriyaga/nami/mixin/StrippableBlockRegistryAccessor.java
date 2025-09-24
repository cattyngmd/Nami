package me.kiriyaga.nami.mixin;

import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(StrippableBlockRegistry.class)
public interface StrippableBlockRegistryAccessor {
    @Invoker("getRegistry")
    static Map<Block, Block> getRegistry() {
        throw new AssertionError();
    }
}