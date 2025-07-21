package me.kiriyaga.nami.feature.module.impl.misc;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixininterface.ISimpleOption;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule(category = "misc")
public class NoResourcepack extends Module {

    public NoResourcepack() {
        super("no resourcepack", "Prevents server to request you any resources.", ModuleCategory.of("misc"), "noresourcepack", "тщкуыщгксузфсл");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onPacketRecieve(PacketReceiveEvent ev) {
        if (MC == null || MC.options == null) return;
        if (ev.getPacket() instanceof ResourcePackSendS2CPacket) {
            ev.cancel();
            MC.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(MC.player.getUuid(), ResourcePackStatusC2SPacket.Status.DECLINED));
        }
    }
}
