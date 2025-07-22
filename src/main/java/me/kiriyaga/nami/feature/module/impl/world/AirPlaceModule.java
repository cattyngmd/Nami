package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@RegisterModule
public class AirPlaceModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("range", 3.0, 2.0, 5.0));
    public final IntSetting delay = addSetting(new IntSetting("delay", 2, 1, 10));
    private final BoolSetting grim = addSetting(new BoolSetting("grim", false));
    private final BoolSetting fluids = addSetting(new BoolSetting("fluids", false));
    private final DoubleSetting lineWidth = addSetting(new DoubleSetting("line width", 1.5, 0.5, 3));
    private final BoolSetting fill = addSetting(new BoolSetting("fill", false));

    public int cooldown = 0;
    private BlockPos renderPos = null;

    public AirPlaceModule() {
        super("air place", "Allows placing blocks mid-air.", ModuleCategory.of("world"), "airplace", "фшкздфсу");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        renderPos = null;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.interactionManager == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (!(MC.crosshairTarget instanceof BlockHitResult hit) || !MC.world.isAir(hit.getBlockPos())) {
            renderPos = null;
            return;
        }

        ItemStack mainStack = MC.player.getMainHandStack();
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof BlockItem)){
            renderPos = null;
            return;
        }

        HitResult ray = MC.player.raycast(range.get(), 1.0f, fluids.get());
        if (!(ray instanceof BlockHitResult target)) {
            renderPos = null;
            return;
        }

        BlockPos targetPos = BlockPos.ofFloored(target.getPos());
        if (!MC.world.isAir(targetPos) || hasEntity(targetPos)) {
            renderPos = null;
            return;
        }

        renderPos = targetPos;

        if (!MC.options.useKey.isPressed()){
            return;
        }
        cooldown = delay.get();

        if (grim.get()) {
            MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));

            MC.interactionManager.interactBlock(MC.player, Hand.OFF_HAND, target);
            MC.player.swingHand(Hand.MAIN_HAND, false);

            MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
            MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        } else {
            MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, target);
            MC.player.swingHand(Hand.MAIN_HAND);
        }
    }


    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null || renderPos == null) return;

        if (!MC.world.isAir(renderPos)) return;
        if (hasEntity(renderPos)) return;

        MatrixStack matrices = event.getMatrices();

        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        Color color = colorModule.getStyledGlobalColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);

        Box box = new Box(renderPos);

        RenderUtil.drawBox(matrices, box, fillColor, color, lineWidth.get().floatValue(), fill.get(), true);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : MC.world.getEntities()) {
            if (entity.getBoundingBox().intersects(new Box(pos))) return true;
        }
        return false;
    }
}
