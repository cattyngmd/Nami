package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.WhitelistSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionTypes;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule(category = "world")
public class AntiInteractModule extends Module {

    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("whitelist", false, this.name));
    public final BoolSetting spawnPoint = addSetting(new BoolSetting("spawn point", true));
    public final BoolSetting packet = addSetting(new BoolSetting("packet", false));

    public AntiInteractModule() {
        super("anti interact", "Prevents you from interacting with certain blocks.", ModuleCategory.of("world"), "antiinteract");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPacketSendRespawn(PacketSendEvent ev) {
        if (!packet.get() || !spawnPoint.get()) return;

        if (!(ev.getPacket() instanceof PlayerInteractBlockC2SPacket interactPacket)) return;
        if (MC.world == null) return;

        BlockPos pos = interactPacket.getBlockHitResult().getBlockPos();
        Block block = MC.world.getBlockState(pos).getBlock();
        var dimension = MC.world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD) ? "overworld"
                : MC.world.getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER) ? "nether"
                : MC.world.getDimensionEntry().matchesKey(DimensionTypes.THE_END) ? "end"
                : "unknown";

        Identifier blockId = Registries.BLOCK.getId(block);

        if (whitelist.get() && whitelist.isWhitelisted(blockId)) {
            ev.cancel();
        }

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
