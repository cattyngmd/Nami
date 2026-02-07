package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import namidevelopment.kiriyaga.api.model.feature.Feature;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterCommand
public class DrawnCommand extends Command {

    public DrawnCommand() {
        super("drawn", new CommandArgument[] {new CommandArgument.FeatureArg("FeatureName")});
    }

    @Override
    public void execute(Object[] args) {
        String input = args[0].toString();

        Feature found = null;
        for (Feature m : FEATURE_SERVICE.getStorage().getAll()) {
            if (m.matches(input)) {
                found = m;
                break;
            }
        }

        if (found == null) {
            CHAT_SERVICE.sendTransient(
                    CAT_FORMAT.format("{gray}Feature {global}" + input + "{gray} not found."));
            return;
        }

        found.setDrawn(!found.isDrawn());
        CHAT_SERVICE.sendTransient(
                CAT_FORMAT.format("{gray}Feature {global}" + input + " {gray}drawn changed."));
    }
}