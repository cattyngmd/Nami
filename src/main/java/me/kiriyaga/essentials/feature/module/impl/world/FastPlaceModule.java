package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketSendEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class FastPlaceModule extends Module {

    public final IntSetting delay = addSetting(new IntSetting("delay", 1, 0, 5));
    public final IntSetting startDelay = addSetting(new IntSetting("start delay", 10, 0, 50));

    public FastPlaceModule() {
        super("fast place", "Decreases cooldown between any type of use.", Category.world, "fastplace");
    }
}
