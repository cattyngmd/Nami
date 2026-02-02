package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.event.impl.ItemEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static namidevelopment.kiriyaga.nami.Nami.EVENT_SERVICE;
import static namidevelopment.kiriyaga.nami.Nami.MC;

@Mixin(UseOnContext.class)
public final class MixinUseOnContext {
    @Inject(method = "getItemInHand", at = @At("RETURN"), cancellable = true)
    public void getStack(final CallbackInfoReturnable<ItemStack> info) {
        if (MC.player == null)
            return;

        ItemEvent event = new ItemEvent();
        EVENT_SERVICE.post(event);

        if (info.getReturnValue().equals(MC.player.getMainHandItem()) && event.isCancelled())
            info.setReturnValue(event.getStack());
    }
}