package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.KeyBindSetting;
import me.kiriyaga.nami.util.KeyUtils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Binds a module to a key. Usage: .bind <module> <key>", "bind", "b", "иштв");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            Text message = CAT_FORMAT.format("Usage: {global}"+COMMAND_MANAGER.getExecutor().getPrefix()+"bind <module> <key>{reset}.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        String moduleName = args[0].toLowerCase();
        String keyName = args[1].toUpperCase();

        Module module = MODULE_MANAGER.getStorage().getAll().stream()
                .filter(m -> m.getName().equalsIgnoreCase(moduleName) || m.matches(moduleName))
                .findFirst()
                .orElse(null);

        if (module == null) {
            Text message = CAT_FORMAT.format("Module {global}" + moduleName + " {reset}not found.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
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
            Text message = CAT_FORMAT.format("Module {global}" + moduleName + " {reset}does not have a keybind setting.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        int keyCode = KeyUtils.parseKey(keyName);

        if (keyCode == -1) {
            Text message = CAT_FORMAT.format("Invalid key name: {global}" + keyName + "{reset}.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
        if (key == null) {
            Text message = CAT_FORMAT.format("Invalid key code: {global}" + keyCode + "{reset}.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        bindSetting.set(keyCode);

        Text message = CAT_FORMAT.format("Bound module {global}" + module.getName() + " {reset}to key {global}" + keyName + "{reset}.");
        CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
    }
}
