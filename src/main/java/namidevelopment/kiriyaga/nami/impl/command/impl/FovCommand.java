package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import namidevelopment.kiriyaga.nami.mixininterface.ISimpleOption;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterCommand
public class FovCommand extends Command {

    public FovCommand() {
        super(
                "fov",
                new CommandArgument[]{
                        new CommandArgument.IntArg("value", 0, 162)
                },
                "fav", "fv"
        );
    }

    @Override
    public void execute(Object[] args) {
        int newFov = (int) args[0];

        ((ISimpleOption)(Object) MC.options.fov()).setValue(newFov);

        CHAT_SERVICE.sendPersistent(FovCommand.class.getName(),
                CAT_FORMAT.format("FOV set to: {g}" + newFov + "{reset}."));
    }
}