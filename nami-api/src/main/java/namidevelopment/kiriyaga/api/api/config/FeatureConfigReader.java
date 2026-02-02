package namidevelopment.kiriyaga.api.api.config;

import com.google.gson.*;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.setting.Setting;

import static namidevelopment.kiriyaga.api.NamiApi.*;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class FeatureConfigReader {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FeatureConfigReader(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void loadFeature(Feature Feature) {
        File file = new File(dirs.getFeatureConfigDir(), Feature.getIdentifier() + ".json");
        if (!file.exists()) {
            API_LOGGER.warn("Feature config not found: " + Feature.getIdentifier());
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
            API_LOGGER.error("Failed to load Feature config: " + Feature.getIdentifier(), e);
        }
    }
}
