package me.kiriyaga.nami.impl.command.impl;

import me.kiriyaga.nami.impl.command.Command;
import me.kiriyaga.nami.impl.command.CommandArgument;
import me.kiriyaga.nami.impl.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class PitchCommand extends Command {

    public PitchCommand() {
        super(
                "pitch",
                new CommandArgument[] {
                        new CommandArgument.IntArg("value", -90, 90)
                },
                "p"
        );
    }

    @Override
    public void execute(Object[] args) {
        int pitch = (int) args[0];

        if (MC.player != null) {
            MC.player.setXRot(pitch);
            CHAT_SERVICE.sendPersistent(PitchCommand.class.getName(),
                    CAT_FORMAT.format("Pitch set to: {g}" + pitch + "{reset}."));
        }
    }
}
