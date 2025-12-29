package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;

@RegisterModule
public class AutoBowReleaseModule extends Module {
    public enum TpsMode {NONE, LATEST, AVERAGE}

    private final IntSetting ticks = addSetting(new IntSetting("Delay", 3, 0, 25));
    private final EnumSetting<TpsMode> tpsMode = addSetting(new EnumSetting<>("TPS", TpsMode.NONE));

    private float ticker = 0f;

    public AutoBowReleaseModule() {
        super("AutoBowRelease", "Automatically releases bow after holding for a set time.", ModuleCategory.of("Combat"), "autbowrelease");
    }

    @Override
    public void onEnable() {
        ticker = 0f;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null || !MC.player.isUsingItem()) return;

        Item usedItem = MC.player.getUseItem().getItem();
        if (usedItem != Items.BOW && usedItem != Items.TRIDENT) return;

        float tps = switch (tpsMode.get()) {
            case LATEST -> SERVER_MANAGER.getLatestTPS();
            case AVERAGE -> SERVER_MANAGER.getAverageTPS();
            default -> 20.0f;
        };

        ticker += tps / 20.0f;

        if (ticker >= ticks.get()) {
            ticker = 0f;

            MC.getConnection().send(
                    new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN)
            );
            MC.player.releaseUsingItem();
            sendSequencedPacket(id -> new ServerboundUseItemPacket(MC.player.getUsedItemHand(), id, ROTATION_MANAGER.getStateHandler().getServerYaw(), ROTATION_MANAGER.getStateHandler().getServerPitch()));

        }
    }
}