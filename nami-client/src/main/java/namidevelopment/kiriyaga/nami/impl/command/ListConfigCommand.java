package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;

import static namidevelopment.kiriyaga.api.NamiApi.*;

@RegisterCommand
public class ListConfigCommand extends Command {

    public ListConfigCommand() {
        super("listconfig",
                new CommandArgument[] {},
                "configlist", "lc");
    }

    @Override
    public void execute(Object[] args) {
        try {
            var configs = CONFIG_SERVICE.getConfigSerializer().listConfigs();
            if (configs.isEmpty()) {
                CHAT_SERVICE.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("No configs found."));
            } else {
                StringBuilder builder = new StringBuilder("Configs: ");
                for (int i = 0; i < configs.size(); i++) {
                    builder.append("{g}").append(configs.get(i)).append("{reset}");
                    if (i < configs.size() - 1) builder.append(", ");
                }
                CHAT_SERVICE.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format(builder.toString()));
            }
        } catch (Exception e) {
            CHAT_SERVICE.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to list configs: {g}" + e + "{reset}."));
        }
    }
}
