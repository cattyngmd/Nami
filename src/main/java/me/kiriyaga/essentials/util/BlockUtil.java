package me.kiriyaga.essentials.util;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;


public class BlockUtil {
    private static final Block AIR_BLOCK = Registries.BLOCK.get(Identifier.of("minecraft", "air"));


    private static final Set<String> NON_VANILLA_GENERATED_IDS = Set.of(
            "minecraft:crafting_table",
            "minecraft:enchanting_table",
            "minecraft:stonecutter",
            "minecraft:loom",
            "minecraft:fletching_table",
            "minecraft:cauldron",
            "minecraft:composter",
            "minecraft:jukebox",
            "minecraft:bell",
            "minecraft:respawn_anchor",
            "minecraft:lodestone",
            "minecraft:conduit",
            "minecraft:beacon",
            "minecraft:end_portal_frame",
            "minecraft:end_gateway",
            "minecraft:command_block",
            "minecraft:chain_command_block",
            "minecraft:repeating_command_block",
            "minecraft:structure_block",
            "minecraft:jigsaw",
            "minecraft:barrier",
            "minecraft:light",
            "minecraft:spawner",
            "minecraft:armor_stand",
            "minecraft:painting",
            "minecraft:daylight_detector",
            "minecraft:note_block",
            "minecraft:redstone_lamp",
            "minecraft:tnt",
            "minecraft:piston",
            "minecraft:sticky_piston",
            "minecraft:observer",
            "minecraft:target",
            "minecraft:tripwire_hook",
            "minecraft:lever",
            "minecraft:redstone_torch",
            "minecraft:repeater",
            "minecraft:comparator",
            "minecraft:crafter",
            "minecraft:chiseled_bookshelf",
            "minecraft:decorated_pot",
            "minecraft:oak_sign",
            "minecraft:spruce_sign",
            "minecraft:birch_sign",
            "minecraft:jungle_sign",
            "minecraft:acacia_sign",
            "minecraft:dark_oak_sign",
            "minecraft:mangrove_sign",
            "minecraft:cherry_sign",
            "minecraft:bamboo_sign",
            "minecraft:crimson_sign",
            "minecraft:warped_sign",
            "minecraft:oak_hanging_sign",
            "minecraft:spruce_hanging_sign",
            "minecraft:birch_hanging_sign",
            "minecraft:jungle_hanging_sign",
            "minecraft:acacia_hanging_sign",
            "minecraft:dark_oak_hanging_sign",
            "minecraft:mangrove_hanging_sign",
            "minecraft:cherry_hanging_sign",
            "minecraft:bamboo_hanging_sign",
            "minecraft:crimson_hanging_sign",
            "minecraft:warped_hanging_sign",
            "minecraft:sculk_catalyst",
            "minecraft:sculk_sensor",
            "minecraft:calibrated_sculk_sensor",
            "minecraft:sculk_shrieker"
    );


    private static final Set<Identifier> NON_VANILLA_GENERATED_IDENTIFIERS = new HashSet<>();

    static {
        for (String id : NON_VANILLA_GENERATED_IDS) {
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null) {
                NON_VANILLA_GENERATED_IDENTIFIERS.add(identifier);
            }
        }
    }

    public static boolean isNonVanillaGenerated(Block block) {
        if (block == null) return false;
        Identifier id = Registries.BLOCK.getId(block);
        return NON_VANILLA_GENERATED_IDENTIFIERS.contains(id);
    }

    public static Set<Block> getNonVanillaGeneratedBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (Identifier id : NON_VANILLA_GENERATED_IDENTIFIERS) {
            Block block = Registries.BLOCK.get(id);
            if (block != null && block != AIR_BLOCK) {
                blocks.add(block);
            }
        }
        return blocks;
    }


    private static final Set<String> STORAGE_IDS = Set.of(
            "minecraft:furnace",
            "minecraft:blast_furnace",
            "minecraft:smoker",
            "minecraft:anvil",
            "minecraft:chipped_anvil",
            "minecraft:damaged_anvil",
            "minecraft:brewing_stand",
            "minecraft:grindstone",
            "minecraft:smithing_table",
            "minecraft:cartography_table",
            "minecraft:lectern",
            "minecraft:chest",
            "minecraft:trapped_chest",
            "minecraft:ender_chest",
            "minecraft:barrel",
            "minecraft:shulker_box",
            "minecraft:white_shulker_box",
            "minecraft:orange_shulker_box",
            "minecraft:magenta_shulker_box",
            "minecraft:light_blue_shulker_box",
            "minecraft:yellow_shulker_box",
            "minecraft:lime_shulker_box",
            "minecraft:pink_shulker_box",
            "minecraft:gray_shulker_box",
            "minecraft:light_gray_shulker_box",
            "minecraft:cyan_shulker_box",
            "minecraft:purple_shulker_box",
            "minecraft:blue_shulker_box",
            "minecraft:brown_shulker_box",
            "minecraft:green_shulker_box",
            "minecraft:red_shulker_box",
            "minecraft:black_shulker_box",
            "minecraft:hopper",
            "minecraft:dispenser",
            "minecraft:dropper"
    );


    private static final Set<Identifier> STORAGE_IDENTIFIERS = new HashSet<>();

    static {
        for (String id : STORAGE_IDS) {
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null) {
                STORAGE_IDENTIFIERS.add(identifier);
            }
        }
    }

    public static boolean isStorage(Block block) {
        if (block == null) return false;
        Identifier id = Registries.BLOCK.getId(block);
        return STORAGE_IDENTIFIERS.contains(id);
    }

    public static Set<Block> getStorage() {
        Set<Block> blocks = new HashSet<>();
        for (Identifier id : STORAGE_IDENTIFIERS) {
            Block block = Registries.BLOCK.get(id);
            if (block != null && block != AIR_BLOCK) {
                blocks.add(block);
            }
        }
        return blocks;
    }
}
