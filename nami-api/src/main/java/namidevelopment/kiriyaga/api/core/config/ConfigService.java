package namidevelopment.kiriyaga.api.core.config;

import com.google.gson.*;
import namidevelopment.kiriyaga.api.core.config.model.ConfigMeta;
import namidevelopment.kiriyaga.api.model.feature.Feature;

import static namidevelopment.kiriyaga.api.NamiApi.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class ConfigService {

    private final ConfigDirectoryProvider dirProvider = new ConfigDirectoryProvider();
    private final FeatureConfigWriter FeatureWriter = new FeatureConfigWriter(dirProvider);
    private final FeatureConfigReader FeatureLoader = new FeatureConfigReader(dirProvider);
    private final ConfigSerializer configSerializer = new ConfigSerializer(dirProvider);
    private final FriendStorage friendStorage = new FriendStorage(dirProvider);
    private final MacroStorage macroStorage = new MacroStorage(dirProvider);

    public ConfigDirectoryProvider getDirectoryProvider() {
        return dirProvider;
    }

    public FeatureConfigWriter getFeatureWriter() {
        return FeatureWriter;
    }

    public FeatureConfigReader getFeatureLoader() {
        return FeatureLoader;
    }

    public ConfigSerializer getConfigSerializer() {
        return configSerializer;
    }

    public FriendStorage getFriendStorage() {
        return friendStorage;
    }

    public MacroStorage getMacroStorage() {
        return macroStorage;
    }

    public void saveFeatures() {
        for (Feature Feature : FEATURE_SERVICE.getStorage().getAll()) {
            FeatureWriter.saveFeature(Feature);
        }
    }

    public void loadFeatures() {
        for (Feature Feature : FEATURE_SERVICE.getStorage().getAll()) {
            FeatureLoader.loadFeature(Feature);
        }
    }

    public void saveConfig(String name, ConfigMode mode) {
        configSerializer.save(name, mode);
    }

    public void loadConfig(String name, ConfigMode mode) {
        configSerializer.load(name, mode);
    }

    public boolean deleteConfig(String name) {
        return configSerializer.delete(name);
    }

    public List<String> listConfigs() {
        return configSerializer.listConfigs();
    }

    public ConfigMeta getConfigMeta(String name) {
        return configSerializer.readMeta(name);
    }

    public void saveFriends(Set<String> friends) {
        friendStorage.save(friends);
    }

    public Set<String> loadFriends() {
        return friendStorage.load();
    }

    public boolean isFriend(String name) {
        return friendStorage.isFriend(name);
    }

    public void saveName(String nameValue) {
        File file = new File(dirProvider.getBaseDir(), "name.json");
        JsonObject json = new JsonObject();
        json.addProperty("name", nameValue);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save name.json", e);
        }
    }

    public String loadName() {
        File file = new File(dirProvider.getBaseDir(), "name.json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("name") ? json.get("name").getAsString() : null;
        } catch (Exception e) {
            LOGGER.error("Failed to load name.json", e);
            return null;
        }
    }

    public void savePrefix(String prefix) {
        File file = new File(dirProvider.getBaseDir(), "prefix.json");
        JsonObject json = new JsonObject();
        json.addProperty("prefix", prefix);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save prefix.json", e);
        }
    }

    public String loadPrefix() {
        File file = new File(dirProvider.getBaseDir(), "prefix.json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("prefix") ? json.get("prefix").getAsString() : null;
        } catch (Exception e) {
            LOGGER.error("Failed to load prefix.json", e);
            return null;
        }
    }

    public void saveMacros() {
        macroStorage.save(MACRO_SERVICE.getAll());
    }

    public void loadMacros() {
        MACRO_SERVICE.clear();
        MACRO_SERVICE.getAll().addAll(macroStorage.load());
    }
}
