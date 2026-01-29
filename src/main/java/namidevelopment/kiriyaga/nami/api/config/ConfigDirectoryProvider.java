package namidevelopment.kiriyaga.nami.api.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

import static namidevelopment.kiriyaga.nami.Nami.NAME;

public class ConfigDirectoryProvider {

    private final File baseDir;

    public ConfigDirectoryProvider() {
        this.baseDir = new File(FabricLoader.getInstance().getGameDir().toFile(), NAME);
    }

    public File getFeatureConfigDir() {
        return new File(baseDir, "config");
    }

    public File getConfigSaveDir() {
        return new File(baseDir, "configs");
    }

    public File getFriendFile() {
        return new File(baseDir, "friends.json");
    }

    public File getBaseDir() {
        return baseDir;
    }
}
