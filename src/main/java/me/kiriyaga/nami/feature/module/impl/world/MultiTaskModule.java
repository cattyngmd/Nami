
package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.InteractionEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.WhitelistSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class MultiTaskModule extends Module {

    public MultiTaskModule() {
        super("multi task", "Allows you using and breaking interaction at the same time.", Category.world, "multitask", "ьгдешефыл");
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    private void onInteractionEvent(InteractionEvent ev) {
        ev.cancel();
    }
}
