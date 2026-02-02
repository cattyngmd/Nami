package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.api.executable.model.ExecutableThreadType;
import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;

@RegisterCommand
public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle",
                new CommandArgument[] {
                        new CommandArgument.FeatureArg("name")
                },
                "on", "off", "switch", "togle", "turnon", "turnoff", "tggle");
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

        EXECUTABLE_SERVICE.getRequestHandler().submit(found::toggle, 0, ExecutableThreadType.PRE_TICK);
    }
}