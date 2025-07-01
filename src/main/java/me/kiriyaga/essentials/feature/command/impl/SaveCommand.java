package me.kiriyaga.essentials.feature.command.impl;

import me.kiriyaga.essentials.feature.command.Command;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

import static me.kiriyaga.essentials.Essentials.*;

public class SaveCommand extends Command {

    public SaveCommand() {
        super("save", "Force config save.", "s", "save", "seva", "sv", "ыфму");
    }

    @Override
    public void execute(String[] args) {
        try {
            CONFIG_MANAGER.save();
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(), "Config has been saved.");
        } catch (Exception e){
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(), "Config has not been saved: §7" + e);
        }
    }
}
