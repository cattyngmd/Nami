package namidevelopment.kiriyaga.nami.impl.command.impl;

import namidevelopment.kiriyaga.nami.impl.command.Command;
import namidevelopment.kiriyaga.nami.impl.command.CommandArgument;
import namidevelopment.kiriyaga.nami.impl.command.RegisterCommand;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.nami.Nami.*;

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
