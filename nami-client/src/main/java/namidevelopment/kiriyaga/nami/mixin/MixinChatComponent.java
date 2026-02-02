package namidevelopment.kiriyaga.nami.mixin;

import namidevelopment.kiriyaga.api.event.impl.ReceiveMessageEvent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Component message, MessageSignature signatureData, GuiMessageTag indicator, CallbackInfo ci) {
        if (signatureData != null) {
            if (CHAT_SERVICE.transientSignature != null && signatureData.equals(CHAT_SERVICE.transientSignature)) {
                return;
            }

            if (CHAT_SERVICE.persistentMessages.containsValue(signatureData)) {
                return;
            }
        }

        ReceiveMessageEvent event = new ReceiveMessageEvent(message, signatureData, indicator);
        EVENT_SERVICE.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
