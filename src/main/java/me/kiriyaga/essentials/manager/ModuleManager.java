package me.kiriyaga.essentials.manager;

import me.kiriyaga.essentials.Essentials;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.essentials.feature.module.impl.client.ColorModule;
import me.kiriyaga.essentials.feature.module.impl.render.ESPModule;
import me.kiriyaga.essentials.feature.module.impl.render.NametagsModule;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.essentials.Essentials.LOGGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        /// RENDER ///
        registerModule(new NametagsModule());
        registerModule(new ESPModule());
        /// CLIENT ///
        registerModule(new ColorModule());
        registerModule(new ClickGuiModule());


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

    public List<Module> getModulesByCategory(Category category) {
        List<Module> modules = new ArrayList<>();
        for (Module mod : MODULE_MANAGER.getModules()) {
            if (mod.getCategory() == category) {
                modules.add(mod);
            }
        }
        modules.sort((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()));
        return modules;
    }
}
