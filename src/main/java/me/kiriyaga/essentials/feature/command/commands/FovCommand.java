package me.kiriyaga.essentials.feature.command.commands;

import me.kiriyaga.essentials.feature.command.Command;
import me.kiriyaga.essentials.mixin.GameOptionsAccessor;
import net.minecraft.client.option.SimpleOption;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class FovCommand extends Command {

    public FovCommand() {
        super("fov", "Changes your FOV. Usage: .fov <Value>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(), "Usage: .fov <value>");
            return;
        }

        try {
            int newFov = Integer.parseInt(args[0].trim());
            if (newFov < 0 || newFov > 420) {
                CHAT_MANAGER.sendPersistent(FovCommand.class.getName(), "FOV must be between 0 and 420.");
                return;
            }

            SimpleOption<Integer> fovOption = ((GameOptionsAccessor) MINECRAFT.options).getFov();
            fovOption.setValue(newFov);


        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(FovCommand.class.getName(), "Invalid number format.");
        }
    }
}
