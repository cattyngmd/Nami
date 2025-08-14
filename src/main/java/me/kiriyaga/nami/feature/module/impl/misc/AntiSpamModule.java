    package me.kiriyaga.nami.feature.module.impl.misc;

    import me.kiriyaga.nami.event.EventPriority;
    import me.kiriyaga.nami.event.SubscribeEvent;
    import me.kiriyaga.nami.event.impl.PostTickEvent;
    import me.kiriyaga.nami.event.impl.PreTickEvent;
    import me.kiriyaga.nami.event.impl.ReceiveMessageEvent;
    import me.kiriyaga.nami.feature.module.Module;
    import me.kiriyaga.nami.feature.module.ModuleCategory;
    import me.kiriyaga.nami.feature.module.RegisterModule;
    import me.kiriyaga.nami.setting.impl.IntSetting;
    import net.minecraft.text.Text;

    import java.util.*;
    import java.util.stream.Collectors;

    import static me.kiriyaga.nami.Nami.*;

    @RegisterModule
    public class AntiSpamModule extends Module {

        private final Map<String, Integer> spamCounts = new HashMap<>();
        private final double SIMILARITY_THRESHOLD = 0.85;

        public final IntSetting amount = addSetting(new IntSetting("amount", 25, 10, 125));

        public AntiSpamModule() {
            super("anti spam", "Automatically combines repeated chat messages.", ModuleCategory.of("misc"), "antispam", "фтешызфь");
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPreTick(PreTickEvent ev) {
            if (MC == null || MC.inGameHud == null || MC.player == null) return;

            List<Text> recentTexts = CHAT_MANAGER.getAllMessages();
            List<ChatMessage> recent = recentTexts.subList(Math.max(0, recentTexts.size() - amount.get()), recentTexts.size())
                    .stream().map(t -> new ChatMessage(t.getString())).collect(Collectors.toList());

            spamCounts.entrySet().removeIf(e -> e.getValue() < 2);

            for (ChatMessage msg : recent) {
                boolean found = false;

                for (String key : new ArrayList<>(spamCounts.keySet())) {
                    double sim = similar(msg.text, key);
                    if (sim >= SIMILARITY_THRESHOLD) {
                        spamCounts.put(key, spamCounts.get(key) + 1);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    spamCounts.put(msg.text, 1);
                }
            }

            for (Map.Entry<String, Integer> entry : spamCounts.entrySet()) {
                String text = entry.getKey();
                int count = entry.getValue();

                if (count <= 1) continue;

                List<Text> toRemove = CHAT_MANAGER.getAllMessages().stream()
                        .filter(m -> similar(m.getString(), text) >= SIMILARITY_THRESHOLD)
                        .collect(Collectors.toList());

                toRemove.stream().map(Text::getString).toList();

                for (Text t : toRemove) {
                    CHAT_MANAGER.removeByText(t.getString());
                    Text newText = CAT_FORMAT.format(text + " {s}x{g}" + count + "{reset}");
                    CHAT_MANAGER.sendPersistent(text, newText, false);
                }
            }
        }

        private double similar(String a, String b) {
            if (a.isEmpty() && b.isEmpty()) return 1.0;

            Map<Character, Integer> countA = new HashMap<>();
            Map<Character, Integer> countB = new HashMap<>();

            for (char c : a.toCharArray()) countA.put(c, countA.getOrDefault(c, 0) + 1);
            for (char c : b.toCharArray()) countB.put(c, countB.getOrDefault(c, 0) + 1);

            int common = 0;
            for (Map.Entry<Character, Integer> entry : countA.entrySet()) {
                char ch = entry.getKey();
                int cnt = entry.getValue();
                if (countB.containsKey(ch)) {
                    common += Math.min(cnt, countB.get(ch));
                }
            }

            int maxLen = Math.max(a.length(), b.length());
            return (double) common / maxLen;
        }

        private static class ChatMessage {
            String text;
            ChatMessage(String text) {
                this.text = text;
            }
        }
    }