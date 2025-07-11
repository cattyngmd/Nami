package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class DisconnectCommand extends Command {

    public DisconnectCommand() {
        super("disconnect", "Disconnects you from the server. Usage: Disconnect", "dis", "discnect", "dissconnect", "logout", "вшысщттусе");
    }

    @Override
    public void execute(String[] args) {
        if (MINECRAFT.player != null && MINECRAFT.getNetworkHandler() != null) {
            MINECRAFT.getNetworkHandler().onDisconnect(new DisconnectS2CPacket(Text.empty()));
        }
    }
}
