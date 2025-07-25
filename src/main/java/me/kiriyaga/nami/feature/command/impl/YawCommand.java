package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class YawCommand extends Command {

    public YawCommand() {
        super("yaw", "Sets player yaw. Usage: .yaw <value>", "y", "нфц");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Usage: {s}" + COMMAND_MANAGER.getExecutor().getPrefix() + "{g}yaw {s}<{g}value{s}>{reset}."));
            return;
        }

        try {
            float yaw = Float.parseFloat(args[0]);

            MC.player.setYaw(yaw);
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Yaw set to: {g}" + yaw + "{reset}."));

        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Invalid number: {g}" + args[0] + "{reset}."));
        }
    }
}