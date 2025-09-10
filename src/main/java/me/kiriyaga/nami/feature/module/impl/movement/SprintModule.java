package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.PlayerInteractEntityC2SPacketAccessor;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class SprintModule extends Module {

    private final BoolSetting keepSprint = addSetting(new BoolSetting("keep sprint", true));
    private final BoolSetting inLiquid = addSetting(new BoolSetting("in liquid", true));

    public SprintModule() {
        super("sprint", "Automatically makes you sprint while moving forward.", ModuleCategory.of("movement"));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUpdateEvent(PreTickEvent event) {
        ClientPlayerEntity player = MC.player;
        if (player == null) return;

        if (!inLiquid.get() && (player.isSubmergedInWater() || player.isTouchingWater()))
            return;


        if (player.forwardSpeed > 0 && !player.hasVehicle()) {
            player.setSprinting(true);
        } else {
            player.setSprinting(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPacketSend(PacketSendEvent event) {
        if (!isEnabled()) return;
        if (!(event.getPacket() instanceof PlayerInteractEntityC2SPacket packet)) return;

        PlayerInteractEntityC2SPacket.InteractTypeHandler handler = ((PlayerInteractEntityC2SPacketAccessor) packet).getTypeHandler();
        PlayerInteractEntityC2SPacket.InteractType type = handler.getType();

        if (type != PlayerInteractEntityC2SPacket.InteractType.ATTACK) return;


        if (!keepSprint.get()) {
            MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            MC.player.setSprinting(false);
        }
    }
}
