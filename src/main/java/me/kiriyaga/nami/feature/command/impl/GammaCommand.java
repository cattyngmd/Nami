package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.mixininterface.ISimpleOption;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.Nami.CAT_FORMAT;

@RegisterCommand
public class GammaCommand extends Command {

    public GammaCommand() {
        super("gamma", "Changes your gamma (brightness). Usage: .gamma <Value>", "light", "brightens", "bright", "пфььф");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            String prefix = COMMAND_MANAGER.getExecutor().getPrefix();
            CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(),
                    CAT_FORMAT.format("Usage: {s}" + prefix + "{g}gamma {s}<{g}value{s}>{reset}."));
            return;
        }

        try {
            double newGamma = Double.parseDouble(args[0].trim());

            if (newGamma < 0.0 || newGamma > 420.0) {
                CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(),
                        CAT_FORMAT.format("Gamma must be between {g}0.0{reset} and {g}420.0{reset}."));
                return;
            }

            ((ISimpleOption) (Object) MC.options.getGamma()).setValue(newGamma);
            CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(),
                    CAT_FORMAT.format("Gamma set to: {g}" + newGamma + "{reset}."));

        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(),
                    CAT_FORMAT.format("Invalid number format."));
        }
    }
}
