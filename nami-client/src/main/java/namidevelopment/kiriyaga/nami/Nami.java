package namidevelopment.kiriyaga.nami;

import namidevelopment.kiriyaga.nami.api.breakprediction.BreakPredictionService;
import namidevelopment.kiriyaga.nami.api.cat.FabricCatFormat;
import namidevelopment.kiriyaga.nami.api.command.CommandService;
import namidevelopment.kiriyaga.nami.api.config.ConfigService;
import namidevelopment.kiriyaga.nami.api.executable.ExecutableService;
import namidevelopment.kiriyaga.nami.api.font.FontService;
import namidevelopment.kiriyaga.nami.api.inventory.InventoryService;
import namidevelopment.kiriyaga.nami.api.macro.MacroService;
import namidevelopment.kiriyaga.nami.api.rotation.RotationService;
import namidevelopment.kiriyaga.nami.impl.gui.newgui.component.NavigatePanelComponent;
import namidevelopment.kiriyaga.nami.impl.gui.newgui.screen.ConfigScreen;
import namidevelopment.kiriyaga.nami.impl.gui.oldgui.screen.ClickGuiScreen;
import namidevelopment.kiriyaga.nami.api.feature.FeatureService;
import namidevelopment.kiriyaga.nami.impl.gui.newgui.screen.FriendScreen;
import namidevelopment.kiriyaga.nami.impl.gui.oldgui.screen.HudEditorScreen;
import namidevelopment.kiriyaga.nami.util.CatStyles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.Tuple;

public class Nami implements ClientModInitializer {
    public static String NAME = "nami";
    public static String DISPLAY_NAME = "Nami";
    public static long START_TIME = 0;
    public static final String VERSION;
    static {
        ModContainer mod = FabricLoader.getInstance().getModContainer("nami-client").orElse(null);
        if (mod != null) {
            VERSION = mod.getMetadata().getVersion().getFriendlyString();
        } else {
            VERSION = "dev-environment";
        }
    }

    public static final Minecraft MC = Minecraft.getInstance();

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static Tuple<ServerAddress, ServerData> LAST_CONNECTION = null;

    public static ClickGuiScreen CLICK_GUI_SCREEN;
    public static HudEditorScreen HUD_EDITOR_SCREEN;
    public static FriendScreen FRIEND_SCREEN;
    public static ConfigScreen CONFIG_SCREEN;
    public static NavigatePanelComponent NAVIGATE_PANEL;



    @Override
    public void onInitializeClient() {
        CLICK_GUI_SCREEN = new ClickGuiScreen();
        HUD_EDITOR_SCREEN = new HudEditorScreen();
        FRIEND_SCREEN = new FriendScreen();
        CONFIG_SCREEN = new ConfigScreen();
        NAVIGATE_PANEL = new NavigatePanelComponent();

        LOGGER.info(NAME + "\n " + VERSION + " has been initialized\n");

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            START_TIME = System.currentTimeMillis();

            CONFIG_SERVICE.loadFeatures();
            CONFIG_SERVICE.loadFriends();
            if (CONFIG_SERVICE.loadName() == null)
                CONFIG_SERVICE.saveName(DISPLAY_NAME);
            else
                DISPLAY_NAME = CONFIG_SERVICE.loadName();
            COMMAND_SERVICE.getExecutor().setPrefix(CONFIG_SERVICE.loadPrefix());
            CONFIG_SERVICE.loadMacros();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CONFIG_SERVICE.saveFeatures();
            CONFIG_SERVICE.saveMacros();
        });

    }
}
