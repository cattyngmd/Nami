package me.kiriyaga.nami.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.impl.feature.Feature;
import me.kiriyaga.nami.impl.setting.Setting;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import static me.kiriyaga.nami.Nami.LOGGER;

public class FeatureConfigWriter {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FeatureConfigWriter(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void saveFeature(Feature Feature) {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", Feature.isEnabled());

        JsonObject settings = new JsonObject();
        for (Setting<?> s : Feature.getSettings()) {
            settings.add(s.getIdentifier(), s.toJson());
        }

        json.add("settings", settings);

        File file = new File(dirs.getFeatureConfigDir(), Feature.getIdentifier() + ".json");
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save Feature config: " + Feature.getIdentifier(), e);
        }
    }
}
