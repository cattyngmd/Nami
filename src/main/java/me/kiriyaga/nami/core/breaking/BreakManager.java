package me.kiriyaga.nami.core.breaking;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.BreakHighlightModule;
import me.kiriyaga.nami.feature.module.impl.world.SpeedMineModule;
import me.kiriyaga.nami.util.render.RenderUtil;
import me.kiriyaga.nami.feature.setting.impl.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.entity.PlayerUtils.isBroken;

public class BreakManager {

    private final Map<UUID, PlayerBreakState> players = new ConcurrentHashMap<>();

    public void init() {
        EVENT_MANAGER.register(this);
        LOGGER.info("Break Manager loaded.");
    }

    public PlayerBreakState get(UUID uuid) {
        return players.computeIfAbsent(uuid, PlayerBreakState::new);
    }
    public Collection<PlayerBreakState> all() {
        return players.values();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        MC.execute(() -> {
        if (event.getPacket() instanceof ClientboundBlockDestructionPacket packet) {
//            if (packet.getProgress() != 0)
//                return;

            int entityId = packet.getId();
            BlockPos pos = packet.getPos();

            Player player = (Player) MC.level.getEntity(entityId);
            if (player == null) return;
            UUID uuid = player.getUUID();
            float speed = MODULE_MANAGER.getStorage().getByClass(SpeedMineModule.class).speed.get().floatValue();
            this.get(uuid).startBreak(pos, Direction.UP, speed);
        }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.level == null) return;

        for (PlayerBreakState state : players.values()) {
            state.onTick();
        }
    }

    @SubscribeEvent
    public void onRender3DEvent(Render3DEvent event) {
        if (MODULE_MANAGER.getStorage().getByClass(BreakHighlightModule.class).isEnabled()) {
            for (PlayerBreakState state : players.values()) {
                state.render(event);
            }
        }
    }
}
