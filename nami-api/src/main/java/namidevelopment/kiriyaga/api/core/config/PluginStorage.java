package namidevelopment.kiriyaga.api.core.config;

import com.google.gson.*;
import namidevelopment.kiriyaga.api.core.config.model.ConfigMeta;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static namidevelopment.kiriyaga.api.NamiApi.LOGGER;

public class PluginStorage {

    private final ConfigDirectoryProvider dirProvider;
    public PluginStorage(ConfigDirectoryProvider dirProvider) {
        this.dirProvider = dirProvider;
    }
    public void save(Map<Integer, Boolean> pluginsState) {
        File file = new File(dirProvider.getBaseDir(), "plugins.json");

        JsonObject root = new JsonObject();
        JsonObject plugins = new JsonObject();
        for (Map.Entry<Integer, Boolean> entry : pluginsState.entrySet()) {
            plugins.addProperty(String.valueOf(entry.getKey()), entry.getValue());
        }

        root.add("plugins", plugins);
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save plugins.json", e);
        }
    }

    public Map<Integer, Boolean> load() {
        File file = new File(dirProvider.getBaseDir(), "plugins.json");
        Map<Integer, Boolean> result = new HashMap<>();
        if (!file.exists()) return result;
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (!root.has("plugins")) return result;
            JsonObject plugins = root.getAsJsonObject("plugins");

            for (String key : plugins.keySet()) {
                int id = Integer.parseInt(key);
                boolean enabled = plugins.get(key).getAsBoolean();
                result.put(id, enabled);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load plugins.json", e);
        }

        return result;
    }
}
