package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(BossHealthOverlay.class)
public abstract class MixinBossHealthOverlay {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled() && noRender.noBossBar.get()) {
            info.cancel();
        }
    }
}