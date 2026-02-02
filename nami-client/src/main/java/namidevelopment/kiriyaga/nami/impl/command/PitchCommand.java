package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;

import static namidevelopment.kiriyaga.nami.Nami.*;

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
