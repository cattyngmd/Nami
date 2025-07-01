package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;
import net.minecraft.text.Text;

import static me.kiriyaga.essentials.Essentials.CHAT_MANAGER;
import static me.kiriyaga.essentials.Essentials.MINECRAFT;

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

            MINECRAFT.player.setYaw(yaw);
            CHAT_MANAGER.sendPersistent(YawCommand.class.getName(), "Yaw set to: §7" + yaw);

        } catch (NumberFormatException e) {
            CHAT_MANAGER.sendPersistent(YawCommand.class.getName(), "Invalid number: §7" + args[0]);
        }
    }
}
