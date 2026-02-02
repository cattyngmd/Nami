package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.nami.api.config.ConfigMode;
import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;

@RegisterCommand
public class LoadConfigCommand extends Command {

    public LoadConfigCommand() {
        super(
                "loadconfig",
                new CommandArgument[]{
                        new CommandArgument.ConfigNameArg("configName"),
                        new CommandArgument.ActionArg(
                                "mode",
                                "all",
                                "settings",
                                "keybind",
                                "color"
                        ) {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        }
                },
                "loadcfg", "lcfg"
        );
    }

    @Override
    public void execute(Object[] args) {
        String configName = args[0].toString();

        String modeArg = args.length > 1 && args[1] != null ? args[1].toString() : "all";

        ConfigMode mode = ConfigMode.valueOf(modeArg.toUpperCase());

        try {
            CONFIG_SERVICE.loadConfig(configName, mode);
            CHAT_SERVICE.sendPersistent(
                    getClass().getName(),
                    CAT_FORMAT.format("Config {g}" + configName + "{reset} loaded with mode {g}" + mode.name().toLowerCase() + "{reset}."));
        } catch (Exception e) {
            CHAT_SERVICE.sendPersistent(
                    getClass().getName(),
                    CAT_FORMAT.format("Failed to load config {g}" + configName + "{reset}: {r}" + e.getMessage() + "{reset}.")
            );
        }
    }
}