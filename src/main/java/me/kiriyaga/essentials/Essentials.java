package me.kiriyaga.essentials;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.events.Render2DEvent;
import me.kiriyaga.essentials.manager.ChatManager;
import me.kiriyaga.essentials.manager.CommandManager;
import me.kiriyaga.essentials.manager.EventManager;
import me.kiriyaga.essentials.manager.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Essentials implements ClientModInitializer {
    public static String NAME = "2bEssentials"; // like that for now, gonna rewrite it while config impl
    public static String VERSION = "0.2";

    public static final MinecraftClient MINECRAFT = MinecraftClient.getInstance();

    public static final EventManager EVENT_MANAGER = new EventManager();
    public static final CommandManager COMMAND_MANAGER = new CommandManager();
    public static final ChatManager CHAT_MANAGER = new ChatManager();
    public static final ModuleManager MODULE_MANAGER = new ModuleManager();
    public static final Logger LOGGER = LogManager.getLogger(NAME);




    @Override
    public void onInitializeClient() {
        COMMAND_MANAGER.init();
        MODULE_MANAGER.init();
        LOGGER.info(NAME + " " + VERSION + " has been initialized");
    }
}
