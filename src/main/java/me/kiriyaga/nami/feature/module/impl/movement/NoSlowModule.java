package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ItemUseSlowEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class NoSlowModule extends Module {
    public enum SlowMode {
        VANILLA, GRIMV3
    }

    public final EnumSetting<SlowMode> mode = addSetting(new EnumSetting<>("Mode", SlowMode.VANILLA));
    public final BoolSetting fastCrawl = addSetting(new BoolSetting("FastCrawl", false));
    //private final BoolSetting fastWeb = addSetting(new BoolSetting("fast web", false));
    private final BoolSetting onlyOnGround = addSetting(new BoolSetting("OnlyOnGround", true));


    public NoSlowModule() {
        super("NoSlow", "Reduces slowdown effect caused on player.", ModuleCategory.of("Movement"), "noslow");
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
        if (mode.get() == SlowMode.GRIMV3){
            boost = MC.player.age % 3 == 0 || MC.player.age % 4 == 0;
            //if (MC.player.age % 12 == 0) boost = false;

            if (boost){
                ev.cancel();
                return;
            }
        }
    }

//    @SubscribeEvent(priority = EventPriority.LOW)
//    private void onPreTick(PreTickEvent event) {
//        if (!fastWeb.get()) return;
//
//        BlockPos webPos = getPhasedWebBlock();
//        if (webPos != null) {
//            //CHAT_MANAGER.sendRaw("c");
//            MC.world.setBlockState(webPos, Blocks.AIR.getDefaultState(), 3);
//        }
//    }

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
