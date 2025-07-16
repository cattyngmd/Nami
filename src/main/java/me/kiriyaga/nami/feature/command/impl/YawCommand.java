package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

public class YawCommand extends Command {

    public YawCommand() {
        super("yaw", "Sets player yaw. Usage: .yaw <Value>", "y", "нфц");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CHAT_MANAGER.sendPersistent(YawCommand.class.getName(), "Usage: .yaw §7<Value>");
            return;
        }

        try {
            float yaw = Float.parseFloat(args[0]);

            MC.player.setYaw(yaw);
            CHAT_MANAGER.sendPersistent(YawCommand.class.getName(), "Yaw set to: §7" + yaw);

        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(YawCommand.class.getName(), "Invalid number: §7" + args[0]);
        }
    }
}
