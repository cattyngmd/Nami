package me.kiriyaga.essentials.manager;

import com.google.gson.*;
import me.kiriyaga.essentials.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.*;

public class ConfigManager {

    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        //.minecraft/config/2bEssentials/config.json
        File configDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "2bEssentials");
        configFile = new File(configDir, "config.json");
    }

    public void load() {
        if (!configFile.exists()) {
            LOGGER.info("Config not found, using defaults.");
            return;
        }

        try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("name")) {
                NAME = root.get("name").getAsString();
            }

            if (root.has("modules")) {
                JsonObject modulesJson = root.getAsJsonObject("modules");

                for (Module module : MODULE_MANAGER.getModules()) {
                    if (!modulesJson.has(module.getName())) continue;
                    JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());

                    if (moduleJson.has("enabled")) {
                        boolean enabled = moduleJson.get("enabled").getAsBoolean();
                        if (enabled != module.isEnabled()) {
                            module.toggle();
                        }
                    }

                    if (moduleJson.has("settings")) {
                        JsonObject settingsJson = moduleJson.getAsJsonObject("settings");

                        for (Setting<?> setting : module.getSettings()) {
                            if (!settingsJson.has(setting.getName())) continue;

                            JsonElement jsonValue = settingsJson.get(setting.getName());
                            setting.fromJson(jsonValue);
                        }
                    }
                }
            }

            LOGGER.info("Config loaded.");
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("name", NAME);

            JsonObject modulesJson = new JsonObject();

            for (Module module : MODULE_MANAGER.getModules()) {
                JsonObject moduleJson = new JsonObject();

                moduleJson.addProperty("enabled", module.isEnabled());

                JsonObject settingsJson = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    settingsJson.add(setting.getName(), setting.toJson());
                }

                moduleJson.add("settings", settingsJson);
                modulesJson.add(module.getName(), moduleJson);
            }

            root.add("modules", modulesJson);

            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }

            LOGGER.info("Config saved.");
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}
