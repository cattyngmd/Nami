package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.KeyBindSetting;
import me.kiriyaga.nami.util.KeyUtils;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterCommand
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

        Module module = MODULE_MANAGER.getStorage().getAll().stream()
                .filter(m -> m.getName().equalsIgnoreCase(moduleName) || m.matches(moduleName))
                .findFirst()
                .orElse(null);

        if (module == null) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Module '§7" + moduleName + "§f' not found.");
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
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Module '§7" + moduleName + "§f' does not have a keybind setting.");
            return;
        }

        int keyCode = KeyUtils.parseKey(keyName);

        if (keyCode == -1) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Invalid key name: §7" + keyName + "§f");
            return;
        }

        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
        if (key == null) {
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Invalid key code: §7" + keyCode + "§f");
            return;
        }

        bindSetting.set(keyCode);
        CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), "Bound module '§7" + module.getName() + "§f' to key '§8" + keyName + "§f'.");
    }
}
