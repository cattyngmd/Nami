package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.core.config.model.PrinterSchematic;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class PrinterCommand extends Command {

    public static BlockPos pos1;
    public static BlockPos pos2;

    private static Map<String, PrinterSchematic> loadedSchematics = new HashMap<>(); // cringe i know

    public PrinterCommand() {
        super(
                "printer",
                new CommandArgument[]{
                        new CommandArgument.ActionArg("pos1/pos2/reset/list/save/load", "pos1", "pos2", "reset", "list", "save", "load"),
                        new CommandArgument.StringArg("param1", 1, 64) {
                            @Override
                            public boolean isRequired() { return false; }
                        },
                        new CommandArgument.StringArg("param2", 1, 64) {
                            @Override
                            public boolean isRequired() { return false; }
                        }
                },
                "print", "p"
        );
    }

    @Override
    public void execute(Object[] args) {
        String action = (String) args[0];

        switch (action) {
            case "pos1" -> {
                pos1 = MC.player.getBlockPos();
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Pos1 set to {g}" + pos1 + "{reset}."));
            }

            case "pos2" -> {
                pos2 = MC.player.getBlockPos();
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Pos2 set to {g}" + pos2 + "{reset}."));
            }

            case "reset" -> {
                pos1 = null;
                pos2 = null;
                loadedSchematics.clear();
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("All states cleared."));
            }

            case "list" -> {
                var list = CONFIG_MANAGER.getPrinterStorage().list();
                if (list.isEmpty()) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("No saved schematics."));
                } else {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Schematics:"));
                    for (String name : list) {
                        CHAT_MANAGER.sendPersistent(getClass().getName(),
                                CAT_FORMAT.format("  {g}" + name + "{reset},"));
                    }
                }
            }

            case "save" -> {
                if (pos1 == null || pos2 == null) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Set pos1 and pos2 first."));
                    return;
                }

                String type = (String) args[1];
                String name = (String) args[2];
                if (name == null) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Specify schematic name."));
                    return;
                }

                PrinterSchematic schematic = PrinterSchematic.capture(pos1, pos2, type);
                CONFIG_MANAGER.getPrinterStorage().save(name, schematic);

                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Schematic saved: {g}" + name + " (" + type + "){reset}."));
            }

            case "load" -> {
                String name = (String) args[1];
                if (name == null) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Specify schematic name."));
                    return;
                }

                PrinterSchematic schematic = CONFIG_MANAGER.getPrinterStorage().load(name);
                if (schematic == null) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Schematic not found: {g}" + name + "{reset}."));
                    return;
                }

                loadedSchematics.put(name, schematic);
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Schematic loaded: {g}" + name + "{reset}."));
            }

            default -> {
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Unknown action: {g}" + action + "{reset}."));
            }
        }
    }

    public static Collection<PrinterSchematic> getLoadedSchematics() {
        return loadedSchematics.values();
    }
}
