package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.api.NamiApi.MC;

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
                CAT_FORMAT.format("{gray}Yaw set to: {global}" + yaw + "{gray}."));
    }
}