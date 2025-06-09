package me.kiriyaga.essentials.mixin;

import me.kiriyaga.essentials.feature.module.impl.render.NoRenderModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {


    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        NoRenderModule noRender = MODULE_MANAGER.getModule(NoRenderModule.class);
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && noRender.isEnabled() && noRender.isNoTotem()) {
            info.cancel();
        }
    }
}