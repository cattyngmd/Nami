package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class PitchCommand extends Command {

    public PitchCommand() {
        super("pitch", "Sets player pitch. Usage: .pitch <value>", "p", "зшце");
    }

    @Override
    public void execute(String[] args) {
        String prefix = COMMAND_MANAGER.getExecutor().getPrefix();

        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(),
                    CAT_FORMAT.format("Usage: {global}" + prefix + "pitch <value>{reset}."));
            return;
        }

        try {
            float pitch = Float.parseFloat(args[0].trim());

            if (MC.player != null) {
                MC.player.setPitch(pitch);
                CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(),
                        CAT_FORMAT.format("Pitch set to: {global}" + pitch + "{reset}."));
            } else {
                CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(),
                        CAT_FORMAT.format("Player is null."));
            }
        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(),
                    CAT_FORMAT.format("Invalid number: {global}" + args[0] + "{reset}."));
        }
    }
}