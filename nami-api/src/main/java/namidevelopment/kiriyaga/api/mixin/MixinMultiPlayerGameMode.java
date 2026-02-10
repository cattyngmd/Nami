package namidevelopment.kiriyaga.api.mixin;

import namidevelopment.kiriyaga.api.mixininterface.IClientPlayerInteractionManager;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements IClientPlayerInteractionManager {

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Override
    public void updateSlot() {
        this.ensureHasSentCarriedItem();
    }
}
