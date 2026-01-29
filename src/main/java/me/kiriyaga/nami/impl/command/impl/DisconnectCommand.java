package me.kiriyaga.nami.impl.command.impl;

import me.kiriyaga.nami.impl.command.Command;
import me.kiriyaga.nami.impl.command.CommandArgument;
import me.kiriyaga.nami.impl.command.RegisterCommand;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.chat.Component;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class DisconnectCommand extends Command {

    public DisconnectCommand() {
        super(
                "disconnect",
                new CommandArgument[0],
                "dis", "discnect", "dissconnect", "logout"
        );
    }

    @Override
    public void execute(Object[] args) {
        if (MC.player != null && MC.getConnection() != null) {
            MC.getConnection().handleDisconnect(new ClientboundDisconnectPacket(Component.empty()));
        }
    }
}
