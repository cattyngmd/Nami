package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.nami.Nami.MC;

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
                    CAT_FORMAT.format("{gray}Pitch set to: {global}" + pitch + "{gray}."));
        }
    }
}
