package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.network.ClientPlayerEntity;

import static me.kiriyaga.nami.Nami.MC;

public class YawModule extends Module {

    private final IntSetting directions = addSetting(new IntSetting("directions", 8, 4, 16));

    public YawModule() {
        super("yaw", "Snap player yaw to nearest fixed angle.", Category.movement, "гфц");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        ClientPlayerEntity player = MC.player;

        float currentYaw = normalizeYaw(player.getYaw());
        int dirCount = directions.get();
        float sector = 360f / dirCount;

        int nearestIndex = Math.round(currentYaw / sector);
        float snappedYaw = normalizeYaw(nearestIndex * sector);

        player.setYaw(snappedYaw);
    }

    private float normalizeYaw(float yaw) {
        yaw %= 360f;
        if (yaw < 0) yaw += 360f;
        return yaw;
    }
}