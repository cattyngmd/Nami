package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ReceiveMessageEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.ChatHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;

//@RegisterModule(category = "misc")
public class AntiSpamModule extends Module {

    private static class SpamData {
        String baseText;
        ChatHudLine line;
        int count;
        MessageSignatureData signature;

        SpamData(String baseText, ChatHudLine line, MessageSignatureData signature) {
            this.baseText = baseText;
            this.line = line;
            this.signature = signature;
            this.count = 1;
        }
    }

    private final Map<String, SpamData> spamMap = new HashMap<>();
    private boolean suppressNext = false;

    public AntiSpamModule() {
        super("anti spam", "Removes repeated chat messages.", ModuleCategory.of("misc"), "antispam", "фтешызфь");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onReceiveMessageEvent(ReceiveMessageEvent event) {
        if (suppressNext) {
            suppressNext = false;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;

        ChatHud chatHud = mc.inGameHud.getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) chatHud;

        List<ChatHudLine> messages = accessor.getMessages();
        if (messages.isEmpty()) return;

        MessageSignatureData eventSignature = event.getSignatureData();
        if (CHAT_MANAGER.isOurSignature(eventSignature)) return;

        String fullText = event.getMessage().getString();
        String baseText = stripCountSuffix(fullText);

        for (Map.Entry<String, SpamData> entry : spamMap.entrySet()) {
            String key = entry.getKey();
            SpamData data = entry.getValue();
            double sim = similarity(key, baseText);

            if (sim >= 0.8) {
                if (data.signature != null) {
                    CHAT_MANAGER.removeSilently(data.signature);
                } else {
                    accessor.getMessages().remove(data.line);
                    accessor.getVisibleMessages().removeIf(visible ->
                            visible.content().toString().equals(data.line.content().getString()));
                    accessor.callRefresh();
                }

                data.count++;
                Text newText = Text.literal(baseText)
                        .append(Text.literal(" (" + data.count + "x)").formatted(Formatting.GRAY));

                suppressNext = true;
                chatHud.addMessage(newText);

                List<ChatHudLine> updatedMessages = accessor.getMessages();
                ChatHudLine newLine = updatedMessages.get(updatedMessages.size() - 1);

                data.line = newLine;
                data.baseText = baseText;
                data.signature = newLine.signature();

                event.cancel();
                return;
            }
        }

        ChatHudLine latestLine = messages.get(messages.size() - 1);
        spamMap.put(baseText, new SpamData(baseText, latestLine, latestLine.signature()));
    }

    private String stripCountSuffix(String text) {
        return text.replaceFirst(" \\(\\d+x\\)$", "");
    }

    private double similarity(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) return 1.0;
        return 1.0 - ((double) levenshtein(a, b) / maxLength);
    }

    private int levenshtein(String a, String b) {
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
