package me.kiriyaga.essentials.feature.module.impl.world;

import me.kiriyaga.essentials.event.EventPriority;
import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.impl.PostTickEvent;
import me.kiriyaga.essentials.event.impl.Render3DEvent;
import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.setting.impl.BoolSetting;
import me.kiriyaga.essentials.setting.impl.DoubleSetting;
import me.kiriyaga.essentials.util.BlockUtil;
import me.kiriyaga.essentials.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static me.kiriyaga.essentials.Essentials.*;

public class EChestFarmerModule extends Module {

    private final BoolSetting render = addSetting(new BoolSetting("Render", true));
    private final DoubleSetting lineWidth = addSetting(new DoubleSetting("Line Width", 1.5, 0.5, 2.5));
    private final BoolSetting filled = addSetting(new BoolSetting("Filled", true));

    public BlockPos targetPos = null;

    public EChestFarmerModule() {
        super("Echest Farmer", "Places and breaks ender chests in front of you.", Category.WORLD, "echestfarmer");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        if (MINECRAFT.player == null || MINECRAFT.world == null) return;

        ClientPlayerEntity player = MINECRAFT.player;

        int echestSlot = findInHotbar(item -> item == Items.ENDER_CHEST);
        int pickaxeSlot = findInHotbar(item -> item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE);

        if (echestSlot == -1 || pickaxeSlot == -1) {
            CHAT_MANAGER.sendPersistent(EChestFarmerModule.class.getName(), "§cMissing ender chest or pickaxe. Disabling module.");
            this.toggle();
            return;
        }

        targetPos = player.getBlockPos().offset(player.getHorizontalFacing());

        Block currentBlock = MINECRAFT.world.getBlockState(targetPos).getBlock();

        if (currentBlock == Blocks.ENDER_CHEST) {
            player.getInventory().setSelectedSlot(pickaxeSlot);
            MINECRAFT.interactionManager.attackBlock(targetPos, Direction.UP);
            player.swingHand(Hand.MAIN_HAND);

        } else {
            player.getInventory().setSelectedSlot(echestSlot);

            Vec3d hitPos = Vec3d.ofCenter(targetPos);
            BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, targetPos, false);

            MINECRAFT.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);

        }
    }

    private int findInHotbar(java.util.function.Predicate<Item> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MINECRAFT.player.getInventory().getStack(i);
            if (stack != null && predicate.test(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(Render3DEvent event) {
        if (!render.get() || MINECRAFT.world == null || MINECRAFT.player == null || targetPos == null)
            return;

        MatrixStack matrices = event.getMatrices();
        BlockState state = MINECRAFT.world.getBlockState(targetPos);
        Color blockColor = BlockUtil.getColorByBlockId(state);

        RenderUtil.drawBlockShape(
                matrices,
                MINECRAFT.world,
                targetPos,
                state,
                new Color(blockColor.getRed(), blockColor.getGreen(), blockColor.getBlue(), 60),
                blockColor,
                lineWidth.get(),
                filled.get()
        );
    }
}
