package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.mixininterface.ISimpleOption;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class FovCommand extends Command {

    public FovCommand() {
        super("fov", "Changes your FOV. Usage: .fov <Value>", "fav", "ащм", "fv");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            String prefix = COMMAND_MANAGER.getExecutor().getPrefix();
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                    CAT_FORMAT.format("Usage: {s}" + prefix + "{g}fov {s}<{g}value{s}>{reset}."));
            return;
        }

        try {
            int newFov = Integer.parseInt(args[0].trim());
            if (newFov < 0 || newFov > 162) {
                CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                        CAT_FORMAT.format("FOV must be between {g}0{reset} and {g}162{reset}."));
                return;
            }
            ((ISimpleOption)(Object) MC.options.getFov()).setValue(newFov);

            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                    CAT_FORMAT.format("FOV set to: {g}" + newFov + "{reset}."));

        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                    CAT_FORMAT.format("Invalid number format."));
        }
    }
}