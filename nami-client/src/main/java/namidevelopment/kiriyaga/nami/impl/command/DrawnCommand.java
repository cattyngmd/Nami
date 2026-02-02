package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;

@RegisterCommand
public class DrawnCommand extends Command {

    public DrawnCommand() {
        super("drawn",
                new CommandArgument[] {
                        new CommandArgument.FeatureArg("FeatureName")
                },
                "draw", "drawFeature", "Featuredraw");
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
                    CAT_FORMAT.format("Feature {g}" + input + "{reset} not found."));
            return;
        }

        found.setDrawn(!found.isDrawn());
        CHAT_SERVICE.sendTransient(
                CAT_FORMAT.format("Feature {g}" + input + " {reset}drawn changed."));
    }
}