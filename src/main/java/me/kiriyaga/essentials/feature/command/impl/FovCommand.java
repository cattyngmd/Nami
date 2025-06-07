package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.mixininterface.ISimpleOption;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class FovCommand extends Command {

    public FovCommand() {
        super("fov", "Changes your FOV. Usage: .fov <Value>", "fav", "ащм", " fv");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(), "Usage: .fov <value>");
            return;
        }

        try {
            int newFov = Integer.parseInt(args[0].trim());
            if (newFov < 0 || newFov > 162) {
                CHAT_MANAGER.sendPersistent(FovCommand.class.getName(), "FOV must be between 0 and 162.");
                return;
            }
            ((ISimpleOption) (Object) MINECRAFT.options.getFov()).setValue(newFov);


        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(), "Invalid number format.");
        }
    }
}
