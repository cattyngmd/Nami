package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterCommand
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

            if (MC.player != null) {
                MC.player.setPitch(pitch);
                CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Pitch set to: §7" + pitch);
            } else {
                CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Player is null.");
            }
        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(), "Invalid number: §7" + args[0]);
        }
    }
}
