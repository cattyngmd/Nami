package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class PitchCommand extends Command {

    public PitchCommand() {
        super("pitch", "Sets player pitch. Usage: .pitch <Value>", "p", "зшце");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Usage: .pitch §7<Value>");
            return;
        }

        try {
            float pitch = Float.parseFloat(args[0]);

            if (MINECRAFT.player != null) {
                MINECRAFT.player.setPitch(pitch);
                CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Pitch set to: §7" + pitch);
            } else {
                CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Player is null.");
            }
        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Invalid number: §7" + args[0]);
        }
    }
}
