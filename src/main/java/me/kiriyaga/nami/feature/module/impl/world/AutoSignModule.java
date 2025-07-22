package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.OpenScreenEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.SignEditScreenAccessor;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoSignModule extends Module {

    private String[] cachedText = null;

    public AutoSignModule() {
        super("auto sign", "Automatically fills signs.", ModuleCategory.of("world"), "sign", "autosign", "фгещышпт");
    }

    @Override
    public void onDisable() {
        cachedText = null;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof UpdateSignC2SPacket packet)
            cachedText = packet.getText();
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        if (!(event.getScreen() instanceof AbstractSignEditScreen screen)) return;
        if (cachedText == null) return;

        SignBlockEntity sign = ((SignEditScreenAccessor) screen).getSign();

        MC.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), true, cachedText[0], cachedText[1], cachedText[2], cachedText[3]));

        event.cancel();
    }
}
