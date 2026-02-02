package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import namidevelopment.kiriyaga.nami.mixininterface.ISimpleOption;

import static namidevelopment.kiriyaga.api.NamiApi.CAT_FORMAT;
import static namidevelopment.kiriyaga.api.NamiApi.CHAT_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterCommand
public class GammaCommand extends Command {

    public GammaCommand() {
        super(
                "gamma",
                new CommandArgument[]{
                        new CommandArgument.IntArg("value", 0, 420)
                },
                "light", "brightens", "bright"
        );
    }

    @Override
    public void execute(Object[] args) {
        int newGamma = (int) args[0];

        ((ISimpleOption) (Object) MC.options.gamma()).setValue((double) newGamma);
        CHAT_SERVICE.sendPersistent(GammaCommand.class.getName(),
                CAT_FORMAT.format("Gamma set to: {g}" + newGamma + "{reset}."));
    }
}