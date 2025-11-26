    package me.kiriyaga.nami.feature.module.impl.miscellaneous;

    import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
    import me.kiriyaga.nami.event.EventPriority;
    import me.kiriyaga.nami.event.SubscribeEvent;
    import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
    import me.kiriyaga.nami.feature.module.Module;
    import me.kiriyaga.nami.feature.module.ModuleCategory;
    import me.kiriyaga.nami.feature.module.RegisterModule;
    import me.kiriyaga.nami.feature.module.impl.visuals.ESPModule;
    import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
    import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
    import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
    import net.minecraft.text.Text;

    import java.util.*;
    import java.util.stream.Collectors;

    import static me.kiriyaga.nami.Nami.*;

    @RegisterModule
    public class AutoIgnore extends Module {

        public enum Mode {
            ignore,
            ignorehard
        }
        public final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.ignore));

        private final WhitelistSetting ignoreList = addSetting(new WhitelistSetting("Ignorelist", false, WhitelistSetting.Type.STRING));

        public AutoIgnore() {
            super("AutoIgnore", "Automatically ignores specified text.", ModuleCategory.of("Miscellaneous"), "antispam");
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPacketReceive(PacketReceiveEvent event) {
            if (MC == null || MC.inGameHud == null || MC.player == null) return;

            if (event.getPacket() instanceof ChatMessageS2CPacket packet) {
                EXECUTABLE_MANAGER.getRequestHandler().submit(() -> {

                    Text textObj = packet.comp_1103();
                    if (textObj == null) return;

                    String message = textObj.getString().toLowerCase();


                    if (!ignoreList.getStringWhitelist().stream().anyMatch(s -> message.contains(s.toLowerCase()))) return;

                    UUID uid = packet.comp_1099();
                    if (uid == null) return;
                    var entry = MC.player.networkHandler.getPlayerListEntry(uid);
                    if (entry == null || entry.getProfile() == null) return;
                    String name = entry.getProfile().getName();
                    if (name == null || name.isEmpty()) return;

                    if (mode.get() == Mode.ignore)
                        MC.player.networkHandler.sendChatCommand("ignorehard " + name);
                    else
                    MC.player.networkHandler.sendChatCommand("ignorehard " + name);

                }, 0, ExecutableThreadType.PRE_TICK);
            }
        }
    }