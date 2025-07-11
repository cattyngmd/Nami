package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.render.NoRenderModule;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(BossBarHud.class)
public abstract class MixinBossBarHud {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(CallbackInfo info) {
        NoRenderModule noRender = MODULE_MANAGER.getModule(NoRenderModule.class);
        if (noRender.isEnabled() && noRender.isNoBossBar()) info.cancel();
    }
}