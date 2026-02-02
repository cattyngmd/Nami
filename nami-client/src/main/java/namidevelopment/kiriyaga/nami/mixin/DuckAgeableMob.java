package namidevelopment.kiriyaga.nami.mixin;

import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AgeableMob.class)
public interface DuckAgeableMob {

    @Accessor("age")
    int Age();

    @Accessor("age")
    void Age(int age);
}
