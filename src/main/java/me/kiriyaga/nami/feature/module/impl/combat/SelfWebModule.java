package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.InteractionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.ENTITY_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class SelfWebModule extends Module {

    public enum PlaceMode { LEGS, HEAD, BOTH }

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    private final EnumSetting<PlaceMode> placeMode = addSetting(new EnumSetting<>("PlaceMode", PlaceMode.LEGS));
    private final BoolSetting selfToggle = addSetting(new BoolSetting("SelfToggle", true));
    private final BoolSetting onlyTarget = addSetting(new BoolSetting("OnlyTarget", false));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 1, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));

    private int cooldown = 0;

    public SelfWebModule() {
        super("SelfWeb", "Automatically places webs on yourself.", ModuleCategory.of("Combat"));
    }

    @SubscribeEvent
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (onlyTarget.get() && ENTITY_MANAGER.getTarget() == null) return;

        int slot = findSlot();
        if (slot == -1) return;

        List<BlockPos> positions = getPositions(MC.player);
        int placed = 0;

        for (BlockPos pos : positions) {
            if (MC.world.getBlockState(pos).isAir()) {
                InteractionUtils.placeBlock(pos, slot, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get());
                placed++;
                if (placed >= shiftTicks.get()) break;
            }
        }

        if (placed > 0) {
            cooldown = delay.get();
        } else if (selfToggle.get()) {
            toggle();
        }
    }

    private int findSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Blocks.COBWEB.asItem()) {
                return i;
            }
        }
        return -1;
    }

    private List<BlockPos> getPositions(PlayerEntity player) {
        double minX = player.getBoundingBox().minX;
        double maxX = player.getBoundingBox().maxX;
        double minZ = player.getBoundingBox().minZ;
        double maxZ = player.getBoundingBox().maxZ;

        int yLegs = (int) Math.floor(player.getY());
        int yHead = (int) Math.floor(player.getY() + 1);

        List<BlockPos> positions = new ArrayList<>();

        if (placeMode.get() == PlaceMode.LEGS || placeMode.get() == PlaceMode.BOTH) {
            positions.add(new BlockPos((int) Math.floor(minX), yLegs, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(minX), yLegs, (int) Math.floor(maxZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yLegs, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yLegs, (int) Math.floor(maxZ)));
        }

        if (placeMode.get() == PlaceMode.HEAD || placeMode.get() == PlaceMode.BOTH) {
            positions.add(new BlockPos((int) Math.floor(minX), yHead, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(minX), yHead, (int) Math.floor(maxZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yHead, (int) Math.floor(minZ)));
            positions.add(new BlockPos((int) Math.floor(maxX), yHead, (int) Math.floor(maxZ)));
        }

        return positions;
    }
}
