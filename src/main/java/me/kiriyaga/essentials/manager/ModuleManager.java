package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.render.ESPModule;
import me.kiriyaga.essentials.feature.module.impl.render.NametagsModule;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.LOGGER;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        registerModule(new NametagsModule());
        registerModule(new ESPModule());

        LOGGER.info("Registered " + modules.size() + " modules");
    }

    public void registerModule(Module module) {
        modules.add(module);
    }

    public void unregisterModule(Module module) {
        modules.remove(module);
        module.setEnabled(false);
    }

    public List<Module> getModules() {
        return modules;
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (clazz.isInstance(module)) {
                return clazz.cast(module);
            }
        }
        return null;
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
