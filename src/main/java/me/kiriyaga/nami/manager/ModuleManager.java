package me.kiriyaga.nami.manager;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.*;
import me.kiriyaga.nami.feature.module.impl.combat.AuraModule;
import me.kiriyaga.nami.feature.module.impl.combat.AutoLogModule;
import me.kiriyaga.nami.feature.module.impl.combat.AutoTotemModule;
import me.kiriyaga.nami.feature.module.impl.misc.*;
import me.kiriyaga.nami.feature.module.impl.movement.*;
import me.kiriyaga.nami.feature.module.impl.render.*;
import me.kiriyaga.nami.feature.module.impl.world.*;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.LOGGER;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        /// RENDER ///
        registerModule(new NametagsModule());
        registerModule(new ESPModule());
        registerModule(new SearchModule());
        registerModule(new NoRenderModule());
        registerModule(new FreeLookModule());
        registerModule(new FreecamModule());
        registerModule(new NoWeatherModule());
        registerModule(new ViewClipModule());
        registerModule(new TracersModule());
        /// CLIENT ///
        registerModule(new ColorModule());
        registerModule(new ClickGuiModule());
        registerModule(new HUDModule());
        registerModule(new PingManagerModule());
        registerModule(new RotationManagerModule());
        registerModule(new EntityManagerModule());

        /// COMBAT ///
        registerModule(new AutoTotemModule());
        registerModule(new AuraModule());
        registerModule(new AutoLogModule());
        /// MOVEMENT ///
        registerModule(new SprintModule());
        registerModule(new GuiMoveModule());
        registerModule(new NoJumpDelayModule());
        registerModule(new SneakModule());
        registerModule(new AutoWalkModule());
        registerModule(new NoRotateModule());
        registerModule(new VelocityModule());
        registerModule(new SprintResetModule());
        registerModule(new ParkourModule());
        registerModule(new SpinnerModule());
        registerModule(new YawModule());
        registerModule(new ElytraFlyModule());
        registerModule(new HighJumpModule());
        /// MISC ///
        registerModule(new JoinAnnounceModule());
        registerModule(new AutoFovModule());
        registerModule(new AutoGammaModule());
        registerModule(new AntiPacketKickModule());
        registerModule(new AutoReconnectModule());
        registerModule(new AutoRespawnModule());
        registerModule(new BetterTabModule());
        registerModule(new NameProtectModule());
        registerModule(new NoSoundLagModule());
        registerModule(new UnfocusedCpuModule());
        /// WORLD ///
        registerModule(new NoBreakDelayModule());
        registerModule(new AntiInteractModule());
        registerModule(new NoHitDelayModule());
        registerModule(new FastPlaceModule());
        registerModule(new ElytraSwapModule());
        registerModule(new AutoElytraModule());
        registerModule(new NoGlitchBlocksModule());
        registerModule(new AutoEatModule());
        registerModule(new MultiTaskModule());
        registerModule(new NoGlitchItemsModule());
        registerModule(new AirPlaceModule());
        registerModule(new AntiSpamModule());

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
