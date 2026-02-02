package namidevelopment.kiriyaga.api.mixin;

import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Animal.class)
public interface DuckAnimal {

    @Accessor("inLove")
    int InLove();

    @Accessor("inLove")
    void InLove(int ticks);
}
