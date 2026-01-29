package namidevelopment.kiriyaga.nami.impl.feature.impl.movement;

import namidevelopment.kiriyaga.nami.event.EventPriority;
import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.ItemUseSlowEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.EnumSetting;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;

import static namidevelopment.kiriyaga.nami.Nami.*;

@RegisterFeature
public class NoSlowFeature extends Feature {
    public enum SlowMode {
        NONE, VANILLA, GRIMV3
    }

    public enum InvMove {
        NONE, WAIT, STOP
    }

    public final EnumSetting<SlowMode> mode = addSetting(new EnumSetting<>("Mode", SlowMode.VANILLA));
    public final EnumSetting<InvMove> invMove = addSetting(new EnumSetting<>("MultiAction", InvMove.NONE));
    public final BoolSetting fastCrawl = addSetting(new BoolSetting("FastCrawl", false));
    //private final BoolSetting fastWeb = addSetting(new BoolSetting("fast web", false));
    private final BoolSetting onlyOnGround = addSetting(new BoolSetting("OnlyOnGround", true));


    public NoSlowFeature() {
        super("NoSlow", "Reduces slowdown effect caused on player.", FeatureCategory.of("Movement"), "noslow");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onSlow(ItemUseSlowEvent ev){
        if (MC.player == null || MC.level == null || !MC.player.isUsingItem() || MC.player.isFallFlying() || MC.player.isHandsBusy())
            return;

        if (onlyOnGround.get() && !MC.player.onGround())
            return;

        if (mode.get() == SlowMode.VANILLA){
            ev.cancel();
            return;
        }

        boolean boost = true; //cattyngmd
        if (mode.get() == SlowMode.GRIMV3){
            boost = MC.player.tickCount % 3 == 0 || MC.player.tickCount % 4 == 0;
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
//            //CHAT_SERVICE.sendRaw("c");
//            MC.world.setBlockState(webPos, Blocks.AIR.getDefaultState(), 3);
//        }
//    }

    private BlockPos getPhasedWebBlock() {
        if (MC.player == null || MC.level == null) return null;

        AABB bb = MC.player.getBoundingBox();

        int minX = Mth.floor(bb.minX);
        int maxX = Mth.ceil(bb.maxX);
        int minY = Mth.floor(bb.minY);
        int maxY = Mth.ceil(bb.maxY);
        int minZ = Mth.floor(bb.minZ);
        int maxZ = Mth.ceil(bb.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (MC.level.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }
}
