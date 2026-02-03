package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.chat.Component;

import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.nami.Nami.MC;

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
