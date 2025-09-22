package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import me.kiriyaga.nami.util.KeyUtils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class BindCommand extends Command {

    public BindCommand() {
        super(
                "bind",
                new CommandArgument[] {
                        new CommandArgument.ModuleArg("module"),
                        new CommandArgument.StringArg("key", 1, 10)
                },
                "b"
        );
    }

    @Override
    public void execute(Object[] parsedArgs) {
        String moduleName = ((String) parsedArgs[0]);
        String keyName = ((String) parsedArgs[1]).toUpperCase();

        Module module = MODULE_MANAGER.getStorage().getAll().stream()
                .filter(m -> m.getName().equalsIgnoreCase(moduleName) || m.matches(moduleName))
                .findFirst()
                .orElse(null);

        if (module == null) {
            Text message = CAT_FORMAT.format("Module {g}" + moduleName + " {reset}not found.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        KeyBindSetting bindSetting = module.getSettings().stream()
                .filter(s -> s instanceof KeyBindSetting)
                .map(s -> (KeyBindSetting) s)
                .findFirst()
                .orElse(null);

        if (bindSetting == null) {
            Text message = CAT_FORMAT.format("Module {g}" + moduleName + " {reset}does not have a keybind setting.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        int keyCode = KeyUtils.parseKey(keyName);

        if (keyCode == -1) {
            Text message = CAT_FORMAT.format("Invalid key name: {g}" + keyName + "{reset}.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
        if (key == null) {
            Text message = CAT_FORMAT.format("Invalid key code: {g}" + keyCode + "{reset}.");
            CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
            return;
        }

        bindSetting.set(keyCode);

        Text message = CAT_FORMAT.format("Bound module {g}" + module.getName() + " {reset}to key {g}" + keyName + "{reset}.");
        CHAT_MANAGER.sendPersistent(BindCommand.class.getName(), message);
    }
}
