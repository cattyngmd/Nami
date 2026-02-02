package namidevelopment.kiriyaga.nami.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface DuckAbstractContainerMenu
{
    @Accessor("stateId")
    void setStateId(final int revisionID);
}