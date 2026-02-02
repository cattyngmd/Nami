package namidevelopment.kiriyaga.nami;

import namidevelopment.kiriyaga.nami.api.*;
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
import namidevelopment.kiriyaga.nami.api.*;
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
        ModContainer mod = FabricLoader.getInstance().getModContainer("nami").orElse(null);
        if (mod != null) {
            VERSION = mod.getMetadata().getVersion().getFriendlyString();
        } else {
            VERSION = "dev-environment";
        }
    }

    public static final Minecraft MC = Minecraft.getInstance();

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final EventService EVENT_SERVICE = new EventService();
    public static final MacroService MACRO_SERVICE = new MacroService();
    public static final ConfigService CONFIG_SERVICE = new ConfigService();
    public static final FeatureService FEATURE_SERVICE = new FeatureService();
    public static final FontService FONT_SERVICE = new FontService();
    public static final ExecutableService EXECUTABLE_SERVICE = new ExecutableService();
    public static final CommandService COMMAND_SERVICE = new CommandService();
    public static final ChatService CHAT_SERVICE = new ChatService();
    public static final FriendService FRIEND_SERVICE = new FriendService(CONFIG_SERVICE);
    public static final RotationService ROTATION_SERVICE = new RotationService();
    public static final InventoryService INVENTORY_SERVICE = new InventoryService();
    public static final ServerService SERVER_SERVICE = new ServerService();
    public static final InputService INPUT_SERVICE = new InputService();
    public static final BreakPredictionService BREAK_SERVICE = new BreakPredictionService();

    public static Tuple<ServerAddress, ServerData> LAST_CONNECTION = null;
    public static FabricCatFormat CAT_FORMAT = new FabricCatFormat();

    public static ClickGuiScreen CLICK_GUI_SCREEN;
    public static HudEditorScreen HUD_EDITOR_SCREEN;
    public static FriendScreen FRIEND_SCREEN;
    public static ConfigScreen CONFIG_SCREEN;
    public static NavigatePanelComponent NAVIGATE_PANEL;



    @Override
    public void onInitializeClient() {
        FEATURE_SERVICE.init();
        COMMAND_SERVICE.init();
        COMMAND_SERVICE.getSuggester().updateDispatcher();
        //FONT_SERVICE.init();
        ROTATION_SERVICE.init();
        INVENTORY_SERVICE.init();
        EXECUTABLE_SERVICE.init();
        SERVER_SERVICE.init();
        CHAT_SERVICE.init();
        INPUT_SERVICE.init();
        BREAK_SERVICE.init();

        CAT_FORMAT.add(new CatStyles());

        CLICK_GUI_SCREEN = new ClickGuiScreen();
        HUD_EDITOR_SCREEN = new HudEditorScreen();
        FRIEND_SCREEN = new FriendScreen();
        CONFIG_SCREEN = new ConfigScreen();
        NAVIGATE_PANEL = new NavigatePanelComponent();

        FRIEND_SERVICE.load();

        LOGGER.info(NAME + "\n " + VERSION + " has been initialized\n");

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            FONT_SERVICE.init(); // font is making glyph textures, it should be after game loaded not on initialize

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
