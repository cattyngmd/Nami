package me.kiriyaga.nami.api.config;

import com.google.gson.*;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.setting.Setting;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

import static me.kiriyaga.nami.Nami.LOGGER;

public class FeatureConfigReader {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FeatureConfigReader(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void loadFeature(Feature Feature) {
        File file = new File(dirs.getFeatureConfigDir(), Feature.getIdentifier() + ".json");
        if (!file.exists()) {
            LOGGER.warn("Feature config not found: " + Feature.getIdentifier());
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("enabled")) {
                boolean enabled = root.get("enabled").getAsBoolean();
                if (enabled != Feature.isEnabled()) {
                    Feature.toggle();
                }
            }

            if (root.has("settings")) {
                JsonObject settingsJson = root.getAsJsonObject("settings");
                for (Setting<?> setting : Feature.getSettings()) {
                    if (settingsJson.has(setting.getIdentifier())) {
                        JsonElement value = settingsJson.get(setting.getIdentifier());
                        setting.fromJson(value);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load Feature config: " + Feature.getIdentifier(), e);
        }
    }
}
