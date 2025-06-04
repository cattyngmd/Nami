package me.kiriyaga.essentials;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.events.Render2DEvent;
import me.kiriyaga.essentials.manager.EventManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Essentials implements ClientModInitializer {
    public static final String NAME = "2bEssentials";
    public static final String VERSION = "0.1";
    public static final MinecraftClient MINECRAFT = MinecraftClient.getInstance();

    public static final EventManager EVENT_MANAGER = new EventManager();
    public static final Logger LOGGER = LogManager.getLogger(NAME);




    @Override
    public void onInitializeClient() {
        LOGGER.info(NAME + " " + VERSION + " has been initialized");
        EVENT_MANAGER.register(this);
    }


    // for test for now
    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String text = "666666666669999999999999";

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        int x = screenWidth / 2 - mc.textRenderer.getWidth(text) / 2;
        int y = screenHeight / 2;

        event.getDrawContext().drawText(mc.textRenderer, text, x, y, 0xFFFFFF, true);
    }
}
