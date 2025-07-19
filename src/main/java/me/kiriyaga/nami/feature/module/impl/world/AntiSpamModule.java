package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ReceiveMessageEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.manager.module.RegisterModule;
import me.kiriyaga.nami.mixin.ChatHudAccessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.network.message.MessageSignatureData;

import java.util.*;

@RegisterModule(category = "world")
public class AntiSpamModule extends Module {

    private static class SpamData {
        ChatHudLine line;
        int count;
        UUID sender;

        SpamData(ChatHudLine line, UUID sender) {
            this.line = line;
            this.count = 1;
            this.sender = sender;
        }
    }

    private final Map<String, SpamData> spamMap = new HashMap<>();
    private final Map<MessageSignatureData, UUID> signatureToSender = new HashMap<>();

    public AntiSpamModule() {
        super("anti spam", "Removes repeated chat messages from the same player.", ModuleCategory.of("world"), "antispam", "фтешызфь");
    }

    public void onNewMessageSignature(MessageSignatureData signature, UUID sender) {
        signatureToSender.put(signature, sender);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onReceiveMessageEvent(ReceiveMessageEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.inGameHud == null) return;

        ChatHud chatHud = mc.inGameHud.getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) chatHud;

        List<ChatHudLine> messages = accessor.getMessages();

        if (messages.isEmpty()) return;

        ChatHudLine latestLine = messages.get(messages.size() - 1);
        String latestText = latestLine.content().getString();
        MessageSignatureData latestSignature = latestLine.signature();
        if (latestSignature == null) return;

        UUID sender = signatureToSender.get(latestSignature);
        if (sender == null) return;

        String baseText = stripCountPrefix(latestText);

        for (Map.Entry<String, SpamData> entry : spamMap.entrySet()) {
            SpamData data = entry.getValue();
            if (!data.sender.equals(sender)) continue;

            if (similarity(entry.getKey(), baseText) >= 0.8) {
                accessor.getMessages().remove(data.line);
                accessor.getVisibleMessages().removeIf(visible -> visible.content().equals(data.line.content()));

                data.count++;

                Text formatted = Text.literal("(" + data.count + "x) ").formatted(Formatting.GRAY)
                        .append(Text.literal(baseText));

                chatHud.addMessage(formatted);

                List<ChatHudLine> updatedMessages = accessor.getMessages();
                data.line = updatedMessages.get(updatedMessages.size() - 1);

                accessor.callRefresh();

                accessor.getMessages().remove(latestLine);
                accessor.getVisibleMessages().removeIf(visible -> visible.content().equals(latestLine.content()));

                return;
            }
        }

        spamMap.put(baseText, new SpamData(latestLine, sender));
    }

    private String stripCountPrefix(String text) {
        if (text.matches("^\\(\\d+x\\) .*")) {
            return text.replaceFirst("^\\(\\d+x\\) ", "");
        }
        return text;
    }

    private double similarity(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) return 1.0;
        return 1.0 - ((double) levenshtein(a, b) / maxLength);
    }

    private int levenshtein(String a, String b) { // this shit is crazy and its from stackoverflow
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}