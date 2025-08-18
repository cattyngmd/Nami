package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.core.rotation.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ItemUseSlowEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class NoSlowModule extends Module {
    public enum SlowMode {
        VANILLA, PARTIAL, ACCEL
    }

    public final EnumSetting<SlowMode> mode = addSetting(new EnumSetting<>("mode", SlowMode.ACCEL));
    public final BoolSetting fastCrawl = addSetting(new BoolSetting("fast crawl", false));
    private final BoolSetting fastWeb = addSetting(new BoolSetting("fast web", false));
    private final BoolSetting onlyOnGround = addSetting(new BoolSetting("only on ground", true));


    public NoSlowModule() {
        super("no slow", "Reduces slowdown effect caused on player.", ModuleCategory.of("movement"), "noslow");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onSlow(ItemUseSlowEvent ev){
        if (MC.player == null || MC.world == null || !MC.player.isUsingItem() || MC.player.isGliding() || MC.player.isRiding())
            return;

        if (onlyOnGround.get() && !MC.player.isOnGround())
            return;

        if (mode.get() == SlowMode.VANILLA){
            ev.cancel();
            return;
        }

        boolean boost = true; //cattyngmd
        if (mode.get() == SlowMode.ACCEL){
            boost = MC.player.age % 3 == 0 || MC.player.age % 4 == 0;
            //if (MC.player.age % 12 == 0) boost = false;

            if (boost){
                ev.cancel();
                return;
            }
        }

        if (mode.get() == SlowMode.PARTIAL) {
            if (MC.player.age % 2 == 0) {
                ev.cancel();
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onPreTick(PreTickEvent event) {
        if (!fastWeb.get()) return;

        BlockPos webPos = getPhasedWebBlock();
        if (webPos != null) {
            //CHAT_MANAGER.sendRaw("c");
            MC.world.setBlockState(webPos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    private BlockPos getPhasedWebBlock() {
        if (MC.player == null || MC.world == null) return null;

        Box bb = MC.player.getBoundingBox();

        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.ceil(bb.maxX);
        int minY = MathHelper.floor(bb.minY);
        int maxY = MathHelper.ceil(bb.maxY);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (MC.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }
}
