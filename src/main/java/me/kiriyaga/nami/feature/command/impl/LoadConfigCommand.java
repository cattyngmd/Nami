package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.core.config.ConfigMode;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

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
            CONFIG_MANAGER.loadConfig(configName, mode);
            CHAT_MANAGER.sendPersistent(
                    getClass().getName(),
                    CAT_FORMAT.format("Config {g}" + configName + "{reset} loaded with mode {g}" + mode.name().toLowerCase() + "{reset}."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(
                    getClass().getName(),
                    CAT_FORMAT.format("Failed to load config {g}" + configName + "{reset}: {r}" + e.getMessage() + "{reset}.")
            );
        }
    }
}