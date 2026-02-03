package namidevelopment.kiriyaga.nami.impl.command;

import namidevelopment.kiriyaga.api.core.executable.model.ExecutableThreadType;
import namidevelopment.kiriyaga.api.util.container.ContainerUtils;
import namidevelopment.kiriyaga.api.model.command.Command;
import namidevelopment.kiriyaga.api.model.command.CommandArgument;
import namidevelopment.kiriyaga.api.annotation.RegisterCommand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

import static namidevelopment.kiriyaga.api.NamiApi.EXECUTABLE_SERVICE;
import static namidevelopment.kiriyaga.api.NamiApi.*;
import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterCommand
public class PeekCommand extends Command {

    public PeekCommand() {
        super(
                "peek",
                new CommandArgument[0],
                "p"
        );
    }

    @Override
    public void execute(Object[] parsedArgs) {
        EXECUTABLE_SERVICE.getRequestHandler().submit(() -> {
            ItemStack main = MC.player.getMainHandItem();
            ItemStack off = MC.player.getOffhandItem();

            if (ContainerUtils.openContainer(main)) return;
            if (ContainerUtils.openContainer(off)) return;
            if (MC.crosshairPickEntity instanceof ItemFrame entity) {
                ContainerUtils.openContainer(entity.getItem());
            }
        }, 5, ExecutableThreadType.PRE_TICK);
    }
}