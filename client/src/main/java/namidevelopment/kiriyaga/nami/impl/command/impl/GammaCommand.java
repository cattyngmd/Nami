package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import namidevelopment.kiriyaga.nami.mixininterface.ISimpleOption;

import static namidevelopment.kiriyaga.nami.Nami.*;

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