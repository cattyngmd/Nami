package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterCommand
public class YawCommand extends Command {

    public YawCommand() {
        super(
                "yaw",
                new CommandArgument[] {
                        new CommandArgument.DoubleArg("value", -180, 180)
                },
                "y"
        );
    }

    @Override
    public void execute(Object[] parsedArgs) {
        double yawDouble = (double) parsedArgs[0];
        float yaw = (float) yawDouble;

        MC.player.setYRot(yaw);
        CHAT_SERVICE.sendPersistent(getClass().getName(),
                CAT_FORMAT.format("Yaw set to: {g}" + yaw + "{reset}."));
    }
}