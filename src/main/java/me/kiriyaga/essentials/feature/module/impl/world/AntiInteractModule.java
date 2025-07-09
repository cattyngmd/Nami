package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PacketSendEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import static me.kiriyaga.essentials.Essentials.MINECRAFT;

public class AntiInteractModule extends Module {

    public final BoolSetting spawnPoint = addSetting(new BoolSetting("spawn point", true));
    public final BoolSetting packet = addSetting(new BoolSetting("packet", false));

    public AntiInteractModule() {
        super("anti interact", "Prevents you from interacting with certain blocks.", Category.world, "antiinteract");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPacketSendRespawn(PacketSendEvent ev) {
        if (!packet.get() || !spawnPoint.get()) return;

        if (!(ev.getPacket() instanceof PlayerInteractBlockC2SPacket interactPacket)) return;

        if (MINECRAFT.world == null) return;

        BlockPos pos = interactPacket.getBlockHitResult().getBlockPos();
        Block block = MINECRAFT.world.getBlockState(pos).getBlock();
        var dimension = MINECRAFT.world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD) ? "overworld"
                : MINECRAFT.world.getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER) ? "nether"
                : MINECRAFT.world.getDimensionEntry().matchesKey(DimensionTypes.THE_END) ? "end"
                : "unknown";

        if (isBed(block) && dimension.equals("overworld")) {
            ev.cancel();
        }

        if (block == Blocks.RESPAWN_ANCHOR && dimension.equals("nether")) {
            ev.cancel();
        }
    }

    public boolean isBed(Block block) {
        return block.toString().toLowerCase().contains("bed");
    }
}
