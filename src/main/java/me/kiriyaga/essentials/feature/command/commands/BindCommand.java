package me.kiriyaga.essentials.feature.command.commands;

import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.settings.KeyBindSetting;
import me.kiriyaga.essentials.utils.KeyUtils;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MODULE_MANAGER;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Binds a module to a key. Usage: .bind <module> <key>", "bind", "b", "иштв");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Usage: .bind <module> <key>");
            return;
        }

        String moduleName = args[0].toLowerCase();
        String keyName = args[1].toUpperCase();

        Module module = MODULE_MANAGER.getModules().stream()
                .filter(m -> m.getName().equalsIgnoreCase(moduleName) || m.matches(moduleName))
                .findFirst()
                .orElse(null);

        if (module == null) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Module '" + moduleName + "' not found.");
            return;
        }

        KeyBindSetting bindSetting = null;

        for (var setting : module.getSettings()) {
            if (setting instanceof KeyBindSetting) {
                bindSetting = (KeyBindSetting) setting;
                break;
            }
        }

        if (bindSetting == null) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Module '" + moduleName + "' does not have a keybind setting.");
            return;
        }

        int keyCode = KeyUtils.parseKey(keyName);

        if (keyCode == -1) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Invalid key name: " + keyName);
            return;
        }

        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
        if (key == null) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Invalid key code: " + keyCode);
            return;
        }

        bindSetting.set(keyCode);
        CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Bound module '" + module.getName() + "' to key '" + keyName + "'.");
    }
}
