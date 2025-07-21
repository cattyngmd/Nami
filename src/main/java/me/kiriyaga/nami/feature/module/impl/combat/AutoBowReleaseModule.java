package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule(category = "combat")
public class AutoBowReleaseModule extends Module {

    private final IntSetting ticks = addSetting(new IntSetting("delay", 20, 1, 20));
    private final BoolSetting tpsSync = addSetting(new BoolSetting("tps sync", false));
    private float tps = 20.0f;

    private float ticker = 0f;

    public AutoBowReleaseModule() {
        super("auto bow release", "Automatically releases bow after holding for a set time.", ModuleCategory.of("combat"));
    }

    @Override
    public void onEnable() {
        ticker = 0f;
        tps = 20.0f;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null || !MC.player.isUsingItem()) return;

        Item usedItem = MC.player.getActiveItem().getItem();
        if (usedItem != Items.BOW && usedItem != Items.TRIDENT) return;

        if (tpsSync.get() && MC.getServer() != null) {
            double tickTimeMs = MC.getServer().getAverageTickTime() / 1_000_000.0;
            tps = (float) Math.min(20.0, 1000.0 / tickTimeMs);
        } else {
            tps = 20.0f;
        }

        ticker += tps / 20.0f;

        if (ticker >= ticks.get()) {
            ticker = 0f;

            // i had some smart fucking key handling and press imitation here, but mc i fucked up so ill just use packets and interactions

            MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            MC.player.stopUsingItem();
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
        }
    }
}
