package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import namidevelopment.kiriyaga.nami.mixininterface.ISimpleOption;

import static namidevelopment.kiriyaga.api.NamiApi.CAT_FORMAT;
import static namidevelopment.kiriyaga.api.NamiApi.CHAT_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.api.NamiApi.MC;

@RegisterCommand
public class FovCommand extends Command {

    public FovCommand() {
        super("fov", new CommandArgument[]{new CommandArgument.IntArg("value", 0, 162)});
    }

    @Override
    public void execute(Object[] args) {
        int newFov = (int) args[0];

        ((ISimpleOption)(Object) MC.options.fov()).setValue(newFov);

        CHAT_SERVICE.sendPersistent(FovCommand.class.getName(),
                CAT_FORMAT.format("{gray}FOV set to: {global}" + newFov + "{gray}."));
    }
}