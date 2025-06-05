package me.kiriyaga.essentials.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;
import static me.kiriyaga.essentials.Essentials.NAME;

public class ChatManager {
    private static final String PREFIX = "§7[§b" + NAME + "§7]";


    private final Map<String, MessageSignatureData> persistentMessages = new HashMap<>();
    private MessageSignatureData transientSignature = null;

    /**
     * Usage:
     * .sendRaw() just sends raw message without tracking, with prefix
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
    public void sendRaw(String message) {
        if (MINECRAFT.inGameHud == null) return;
        getChatHud().addMessage(Text.literal(PREFIX + message));
    }

    public void sendPersistent(String key, String message) {
        ChatHud chatHud = getChatHud();

        if (persistentMessages.containsKey(key)) {
            chatHud.removeMessage(persistentMessages.get(key));
        }

        Text text = Text.literal(PREFIX + message);
        MessageSignatureData signature = null;
        MessageIndicator indicator = MessageIndicator.system();

        chatHud.addMessage(text, signature, indicator);
        persistentMessages.put(key, signature);
    }

    public void sendTransient(String message) {
        ChatHud chatHud = getChatHud();

        if (transientSignature != null) {
            chatHud.removeMessage(transientSignature);
            transientSignature = null;
        }

        Text text = Text.literal(PREFIX + message);
        MessageSignatureData signature = null;
        MessageIndicator indicator = MessageIndicator.system();

        chatHud.addMessage(text, signature, indicator);
        transientSignature = signature;
    }

    public void removePersistent(String key) {
        ChatHud chatHud = getChatHud();
        if (persistentMessages.containsKey(key)) {
            chatHud.removeMessage(persistentMessages.get(key));
            persistentMessages.remove(key);
        }
    }

    public void clear() {
        ChatHud chatHud = getChatHud();
        for (MessageSignatureData sig : persistentMessages.values()) {
            chatHud.removeMessage(sig);
        }
        persistentMessages.clear();

        if (transientSignature != null) {
            chatHud.removeMessage(transientSignature);
            transientSignature = null;
        }
    }

    private ChatHud getChatHud() {
        return MINECRAFT.inGameHud.getChatHud();
    }
}