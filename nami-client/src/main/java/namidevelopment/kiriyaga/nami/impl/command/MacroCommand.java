package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.nami.api.macro.model.Macro;
import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import namidevelopment.kiriyaga.api.util.KeyUtils;

@RegisterCommand
public class MacroCommand extends Command {

    public MacroCommand() {
        super(
                "macro",
                new CommandArgument[]{
                        new CommandArgument.ActionArg("add/del/list", "add", "del", "list"),
                        new CommandArgument.StringArg("key", 1, 16){
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        },
                        new CommandArgument.StringArg("message", 1, 256) {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }

                            @Override
                            public Object parse(String[] input, int index) {
                                StringBuilder builder = new StringBuilder();
                                for (int i = index; i < input.length; i++) {
                                    builder.append(input[i]);
                                    if (i != input.length - 1) builder.append(" ");
                                }
                                return builder.toString();
                            }
                        }
                },
                "mac", "m"
        );
    }

    @Override
    public void execute(Object[] args) {
        String action = (String) args[0];

        switch (action) {
            case "add" -> {
                String keyName = ((String) args[1]).toUpperCase();
                String message = (String) args[2];

                int keyCode = KeyUtils.parseKey(keyName);
                if (keyCode == -1) {
                    CHAT_SERVICE.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Invalid key: {g}" + keyName + "{reset}."));
                    return;
                }

                //MACRO_SERVICE.removeMacro(keyCode);
                MACRO_SERVICE.addMacro(new Macro(keyCode, message));
                CONFIG_SERVICE.saveMacros();

                CHAT_SERVICE.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Macro added: {g}" + keyName + " " + message + "{reset}."));
            }

            case "del" -> {
                String keyName = ((String) args[1]).toUpperCase();
                int keyCode = KeyUtils.parseKey(keyName);
                if (keyCode == -1) {
                    CHAT_SERVICE.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Invalid key: {g}" + keyName + "{reset}."));
                    return;
                }

                MACRO_SERVICE.removeMacro(keyCode);
                CONFIG_SERVICE.saveMacros();

                CHAT_SERVICE.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Macro removed: {g}" + keyName + "{reset}."));
            }

            case "list" -> {
                if (MACRO_SERVICE.getAll().isEmpty()) {
                    CHAT_SERVICE.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("No macros have been added."));
                    return;
                }

                StringBuilder builder = new StringBuilder();
                builder.append("Macros:\n");

                for (Macro macro : MACRO_SERVICE.getAll()) {
                    String key = KeyUtils.getKeyName(macro.getKeyCode());
                    String msg = macro.getMessage();
                    builder.append("  {g}")
                            .append(key)
                            .append(" ")
                            .append(msg)
                            .append("{reset}\n");
                }

                CHAT_SERVICE.sendPersistent(
                        getClass().getName(),
                        CAT_FORMAT.format(builder.toString())
                );
            }

            default -> {
                CHAT_SERVICE.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Unknown action: {g}" + action + "{reset}."));
            }
        }
    }
}
