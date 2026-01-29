package me.kiriyaga.nami.impl.command.impl;

import me.kiriyaga.nami.api.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.impl.command.Command;
import me.kiriyaga.nami.impl.command.CommandArgument;
import me.kiriyaga.nami.impl.command.RegisterCommand;
import me.kiriyaga.nami.util.container.ContainerUtils;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

import static me.kiriyaga.nami.Nami.*;

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