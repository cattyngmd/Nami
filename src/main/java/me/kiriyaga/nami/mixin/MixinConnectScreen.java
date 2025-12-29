package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.Nami;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Tuple;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {

    // TODO(Ravel): target method connect is ambiguous
    @Inject(method = "connect", at = @At("HEAD"))
    private static void onConnect(
            Screen screen,
            Minecraft client,
            ServerAddress address,
            ServerData info,
            boolean quickPlay,
            TransferState cookieStorage, // 1.21.5 loved
            CallbackInfo ci
    ) {
        Nami.LAST_CONNECTION = new Tuple<>(address, info);
    }
}

