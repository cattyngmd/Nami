package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.mixin.ChatHudAccessor;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;
import static me.kiriyaga.essentials.Essentials.NAME;

public class ChatManager {
    /**
     * Usage:
     * .sendRaw() just sends raw message without tracking
     *
     * .sendPersistent() non timed/replaced by default message, but is tracked and we can delete it by its key
     * Why? Because we want to track session joins/leaves, for example, and clean "outdated" info, but without
     * affecting other messages. Can be replaced by key.
     *
     * .removePersistent() all by its name
     *
     * .sendTransient() transient message, that will be deleted and replaced while new transient message appears     *
     *
     * .clear() clears all messages (contained by us btw)
     *
     */

    private final Map<String, MessageSignatureData> persistentMessages = new HashMap<>();
    private MessageSignatureData transientSignature = null;

    private MessageSignatureData generateSignature() {
        byte[] data = new byte[256];
        new SecureRandom().nextBytes(data);
        return new MessageSignatureData(data);
    }



    public void sendRaw(String message) {
        sendRaw(message, true);
    }

    public void sendRaw(String message, boolean prefix) {
        if (MINECRAFT.inGameHud == null) return;
        getChatHud().addMessage(Text.literal(prefix() + message));
    }

    public void sendPersistent(String key, String message) {
        ChatHud chatHud = getChatHud();

        if (persistentMessages.containsKey(key)) {
            removeSilently(persistentMessages.get(key));
        }

        Text text = Text.literal(prefix() + message);
        MessageSignatureData signature = generateSignature();
        MessageIndicator indicator = MessageIndicator.system();

        chatHud.addMessage(text, signature, indicator);
        persistentMessages.put(key, signature);
    }

    public void sendTransient(String message) {
        ChatHud chatHud = getChatHud();

        if (transientSignature != null) {
            removeSilently(transientSignature);
            transientSignature = null;
        }

        Text text = Text.literal(prefix() + message);
        MessageSignatureData signature = generateSignature();
        MessageIndicator indicator = MessageIndicator.system();

        chatHud.addMessage(text, signature, indicator);
        transientSignature = signature;
    }

    public void removePersistent(String key) {
        ChatHud chatHud = getChatHud();
        if (persistentMessages.containsKey(key)) {
            removeSilently(persistentMessages.get(key));
            persistentMessages.remove(key);
        }
    }

    public void clear() {
        ChatHud chatHud = getChatHud();
        for (MessageSignatureData sig : persistentMessages.values()) {
            chatHud.removeMessage(sig);
            removeSilently(sig);
        }
        persistentMessages.clear();

        if (transientSignature != null) {
            removeSilently(transientSignature);
            transientSignature = null;
        }
    }

    private ChatHud getChatHud() {
        return MINECRAFT.inGameHud.getChatHud();
    }

    // btw this is kinda shitcode, but im doin this because of minecraft spaghetti code

    private void removeSilently(MessageSignatureData signature) {
        if (MINECRAFT.inGameHud == null) return;

        ChatHud hud = MINECRAFT.inGameHud.getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) hud;

        accessor.getMessages().removeIf(line -> signature.equals(line.signature()));

        accessor.getVisibleMessages().removeIf(visible -> {
            for (ChatHudLine line : accessor.getMessages()) {
                if (signature.equals(line.signature())) {
                    return visible.content().equals(line.content());
                }
            }
            return false;
        });
        accessor.callRefresh();
    }

    private String prefix(){
        return "§7[§b" + NAME + "§7] §f";
    }
}
