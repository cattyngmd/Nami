package me.kiriyaga.nami.impl.command.impl;

import me.kiriyaga.nami.api.config.ConfigMode;
import me.kiriyaga.nami.impl.command.Command;
import me.kiriyaga.nami.impl.command.CommandArgument;
import me.kiriyaga.nami.impl.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class SaveConfigCommand extends Command {

    public SaveConfigCommand() {
        super(
                "saveconfig",
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
                "savecfg", "scfg"
        );
    }

    @Override
    public void execute(Object[] args) {
        String configName = args[0].toString();

        String modeArg = args.length > 1 && args[1] != null ? args[1].toString() : "all";
        ConfigMode mode = ConfigMode.valueOf(modeArg.toUpperCase());

        try {
            CONFIG_SERVICE.saveConfig(configName, mode);
            CHAT_SERVICE.sendPersistent(
                    getClass().getName(),
                    CAT_FORMAT.format("Config {g}" + configName + "{reset} saved with {g}" + mode.name().toLowerCase() + "{reset}."));
        } catch (Exception e) {
            CHAT_SERVICE.sendPersistent(
                    getClass().getName(),
                    CAT_FORMAT.format(
                            "Failed to save config {g}" + configName + "{reset}: {g}" + e + "{reset}."
                    )
            );
        }
    }
}
