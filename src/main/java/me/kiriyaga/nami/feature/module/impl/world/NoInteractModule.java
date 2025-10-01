package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.event.impl.PlaceBlockEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.Map;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoInteractModule extends Module {

    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("WhiteList", false, WhitelistSetting.Type.BLOCK));
    public final BoolSetting spawnPoint = addSetting(new BoolSetting("SpawnPoint", true));
    public final BoolSetting strip = addSetting(new BoolSetting("Strip", false));
    public final BoolSetting packet = addSetting(new BoolSetting("Packet", false));

    public NoInteractModule() {
        super("NoInteract", "Prevents you from interacting with certain blocks.", ModuleCategory.of("World"), "antiinteract");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPlaceBlock(PlaceBlockEvent event) {
        ClientPlayerEntity player = event.getPlayer();
        BlockHitResult hitResult = event.getHitResult();

        if (player.getWorld() == null) return;

        Block block = player.getWorld().getBlockState(hitResult.getBlockPos()).getBlock();
        String dimension = player.getWorld().getDimension().toString();

        Identifier blockId = Registries.BLOCK.getId(block);

        if (whitelist.get() && whitelist.isWhitelisted(blockId)) {
            event.cancel();
            return;
        }

        if (spawnPoint.get()) {
            if (player.getWorld().getDimension().comp_648() && isBed(block)) {
                event.cancel();
                return;
            }

            if (block == Blocks.RESPAWN_ANCHOR && dimension.contains("nether")) {
                event.cancel();
                return;
            }
        }

        if (strip.get()) {
            boolean isMain = MC.player.getMainHandStack().isIn(ItemTags.AXES) && event.getHand() == Hand.MAIN_HAND;
            boolean isOff = MC.player.getOffHandStack().isIn(ItemTags.AXES) && event.getHand() == Hand.OFF_HAND;
            if (isMain || isOff) {

                Map<Block, Block> strippables = net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils.getAsMutableMap(
                        () -> net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor.getStrippedBlocks(),
                        map -> net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor.setStrippedBlocks(map)
                );

                if (strippables.containsKey(block)) {
                    event.cancel();
                    return;
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onPacketSendRespawn(PacketSendEvent ev) {
        if (!packet.get()) return;

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
            return;
        }

        if (spawnPoint.get()) {
            if (isBed(block) && dimension.equals("overworld")) {
                ev.cancel();
                return;
            }

            if (block == Blocks.RESPAWN_ANCHOR && dimension.equals("nether")) {
                ev.cancel();
                return;
            }
        }

        if (strip.get()) {
            boolean isMain = MC.player.getMainHandStack().isIn(ItemTags.AXES) && interactPacket.getHand() == Hand.MAIN_HAND;
            boolean isOff = MC.player.getOffHandStack().isIn(ItemTags.AXES) && interactPacket.getHand() == Hand.OFF_HAND;

            if (isMain || isOff) { // this is fucking shizo
                Map<Block, Block> strippables = net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils.getAsMutableMap(
                        () -> net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor.getStrippedBlocks(),
                        map -> net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor.setStrippedBlocks(map)
                );

                if (strippables.containsKey(block)) {
                    ev.cancel();
                    return;
                }
            }
        }

    }

    public boolean isBed(Block block) {
        return block.toString().toLowerCase().contains("bed");
    }
}
