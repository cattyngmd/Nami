package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.nami.Nami;
import net.minecraft.client.Minecraft;
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
            Minecraft minecraft, ServerAddress serverAddress, ServerData serverData, TransferState transferState, CallbackInfo ci
            // 1.21.5 loved
    ) {
        Nami.LAST_CONNECTION = new Tuple<>(serverAddress, serverData);
    }
}

