package me.kiriyaga.nami;

import me.kiriyaga.nami.feature.gui.ClickGuiScreen;
import me.kiriyaga.nami.manager.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.Pair;

public class Nami implements ClientModInitializer {
    public static String NAME = "Nami 波";
    public static String DISPLAY_NAME = "Nami 波";
    public static final String VERSION;
    static {
        ModContainer mod = FabricLoader.getInstance().getModContainer("nami").orElse(null);
        if (mod != null) {
            VERSION = mod.getMetadata().getVersion().getFriendlyString();
        } else {
            VERSION = "dev-environment";
        }
    }

    public static final MinecraftClient MINECRAFT = MinecraftClient.getInstance();

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final EventManager EVENT_MANAGER = new EventManager();
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();
    public static final ModuleManager MODULE_MANAGER = new ModuleManager();
    public static final EntityManager ENTITY_MANAGER = new EntityManager();
    public static final CommandManager COMMAND_MANAGER = new CommandManager();
    public static final ChatManager CHAT_MANAGER = new ChatManager();
    public static final FriendManager FRIEND_MANAGER = new FriendManager();
    public static final PingManager PING_MANAGER = new PingManager();
    public static final RotationManager ROTATION_MANAGER = new RotationManager();
    public static Pair<ServerAddress, ServerInfo> LAST_CONNECTION = null;

    public static ClickGuiScreen CLICK_GUI = new ClickGuiScreen();



    @Override
    public void onInitializeClient() {
        COMMAND_MANAGER.init();
        MODULE_MANAGER.init();
        PING_MANAGER.init();
        ROTATION_MANAGER.init();
        ENTITY_MANAGER.init();

        CONFIG_MANAGER.load();
        FRIEND_MANAGER.load();

        LOGGER.info(NAME + " " + VERSION + " has been initialized");

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CONFIG_MANAGER.save();
        });

    }
}
