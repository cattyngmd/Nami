package namidevelopment.kiriyaga.api.core;

import namidevelopment.kiriyaga.api.model.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class PluginService {

    private final Map<Integer, Plugin> plugins = new HashMap<>();

    public void registerPlugin(Plugin plugin) {
        plugins.put(plugin.getId(), plugin);
        LOGGER.info("Registered plugin: " + plugin.getName() + " v" + plugin.getVersion());
    }

    public void unregisterPlugin(int id) {
        Plugin plugin = plugins.remove(id);
        if (plugin != null) {
            LOGGER.info("Unregistered plugin: " + plugin.getName());
        }
    }

    public Plugin getPlugin(int id) {
        return plugins.get(id);
    }

    public void enablePlugin(int id) {
        Plugin plugin = plugins.get(id);
        if (plugin == null) return;

        if (plugin.isEnabled()) {
            LOGGER.warn("Plugin already enabled: " + plugin.getName());
            return;
        }

        plugin.getRegisteredFeatures().forEach(FEATURE_SERVICE::registerFeature);
        plugin.getRegisteredCommands().forEach(COMMAND_SERVICE::addCommand);

        plugin.setEnabled(true);
        LOGGER.info("Enabled plugin: " + plugin.getName());
    }

    public void disablePlugin(int id) {
        Plugin plugin = plugins.get(id);
        if (plugin == null) return;

        if (!plugin.isEnabled()) {
            LOGGER.warn("Plugin already disabled: " + plugin.getName());
            return;
        }

        plugin.getRegisteredCommands().forEach(COMMAND_SERVICE::removeCommand);
        plugin.getRegisteredFeatures().forEach(FEATURE_SERVICE::unregisterFeature);

        plugin.setEnabled(false);
        LOGGER.info("Disabled plugin: " + plugin.getName());
    }

    public Map<Integer, Boolean> getPluginsState() {
        Map<Integer, Boolean> state = new HashMap<>();

        for (Plugin plugin : plugins.values()) {
            state.put(plugin.getId(), plugin.isEnabled());
        }

        return state;
    }

    public void applyPluginsState(Map<Integer, Boolean> state) {
        for (Map.Entry<Integer, Boolean> entry : state.entrySet()) {
            int id = entry.getKey();
            boolean enabled = entry.getValue();

            if (!plugins.containsKey(id)) continue;

            if (enabled) enablePlugin(id);
            else disablePlugin(id);
        }
    }

    public Map<Integer, Plugin> getPlugins() {
        return plugins;
    }
    public Iterable<Plugin> getAllPlugins() {
        return plugins.values();
    }
}
