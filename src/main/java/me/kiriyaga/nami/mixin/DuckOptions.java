package me.kiriyaga.nami.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(Options.class)
public interface DuckOptions {

    @Accessor("fov")
    OptionInstance<Integer> getFov();

    @Accessor("gamma")
    OptionInstance<Double> getGamma();

    @Accessor("modelParts")
    Set<PlayerModelPart> getPlayerModelParts();

    @Accessor("modelParts")
    void setPlayerModelParts(Set<PlayerModelPart> parts);
}
