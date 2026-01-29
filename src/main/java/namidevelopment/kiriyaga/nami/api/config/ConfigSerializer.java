package namidevelopment.kiriyaga.nami.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import namidevelopment.kiriyaga.nami.api.config.model.ConfigMeta;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.setting.Setting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static namidevelopment.kiriyaga.nami.Nami.*;

public class ConfigSerializer {

    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigSerializer(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void save(String configName, ConfigMode mode) {
        JsonObject root = new JsonObject();

        JsonObject meta = new JsonObject();
        meta.addProperty("author", MC.getUser().getName());
        meta.addProperty("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.addProperty("client", NAME);
        meta.addProperty("version", VERSION);
        meta.addProperty("mode", mode.name());
        root.add("meta", meta);
        JsonObject Features = new JsonObject();

        for (Feature m : FEATURE_SERVICE.getStorage().getAll()) {
            JsonObject mod = new JsonObject();
            mod.addProperty("enabled", m.isEnabled());

            JsonObject settings = new JsonObject();
            for (Setting<?> s : m.getSettings()) {
                if (!mode.accept(s)) continue;
                settings.add(s.getIdentifier(), s.toJson());
            }

            mod.add("settings", settings);
            Features.add(m.getIdentifier(), mod);
        }

        root.add("Features", Features);

        File file = new File(dirs.getConfigSaveDir(), configName + ".json");
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save config " + configName, e);
        }
    }

    public void load(String configName, ConfigMode mode) {
        File file = new File(dirs.getConfigSaveDir(), configName + ".json");
        if (!file.exists()) {
            LOGGER.warn("Config file not found: " + configName);
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject Features = root.getAsJsonObject("Features");

            for (Feature m : FEATURE_SERVICE.getStorage().getAll()) {
                if (!Features.has(m.getIdentifier())) continue;

                JsonObject mod = Features.getAsJsonObject(m.getIdentifier());

                if (mod.has("enabled")) {
                    boolean enabled = mod.get("enabled").getAsBoolean();
                    if (enabled != m.isEnabled()) {
                        m.toggle();
                    }
                }

                JsonObject settings = mod.getAsJsonObject("settings");
                if (settings == null) continue;

                for (Setting<?> s : m.getSettings()) {
                    if (!mode.accept(s)) continue;
                    if (settings.has(s.getIdentifier())) {
                        s.fromJson(settings.get(s.getIdentifier()));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config " + configName, e);
        }
    }

    public boolean delete(String configName) {
        File file = new File(dirs.getConfigSaveDir(), configName + ".json");
        if (!file.exists()) {
            LOGGER.warn("Config file not found for delete: " + configName);
            return false;
        }
        return file.delete();
    }

    public List<String> listConfigs() {
        File dir = dirs.getConfigSaveDir();
        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        return Arrays.stream(dir.listFiles((d, name) -> name.endsWith(".json"))).map(f -> f.getName().replaceFirst("\\.json$", "")).collect(Collectors.toList());
    }

    public ConfigMeta readMeta(String configName) {
        File file = new File(dirs.getConfigSaveDir(), configName + ".json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("meta")) return null;

            JsonObject meta = root.getAsJsonObject("meta");
            ConfigMode mode = ConfigMode.ALL;
            if (meta.has("mode")) {
                try {
                    mode = ConfigMode.valueOf(meta.get("mode").getAsString());
                } catch (IllegalArgumentException ignored) {
                }
            }

            return new ConfigMeta(meta.has("author") ? meta.get("author").getAsString() : "unknown", meta.has("version") ? meta.get("version").getAsString() : "unknown", meta.has("createdAt") ? LocalDateTime.parse(meta.get("createdAt").getAsString()) : null, mode);
        } catch (Exception e) {
            LOGGER.error("Failed to read meta for " + configName, e);
            return null;
        }
    }
}
