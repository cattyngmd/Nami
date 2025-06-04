package me.kiriyaga.essentials;

import me.kiriyaga.essentials.manager.EventManager;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Essentials implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("2bEssentials");
    public static final String NAME = "2bEssentials";
    public static final String VERSION = "0.1";
    public static final EventManager EVENT_MANAGER = new EventManager();



    @Override
    public void onInitializeClient() {

        LOGGER.info(NAME + " " + VERSION + " has been initialized");
    }
}
