package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.OpenScreenEvent;
import me.kiriyaga.nami.event.impl.PacketSendEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.SignEditScreenAccessor;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoSignModule extends Module {

    private final IntSetting delay = addSetting(new IntSetting("Delay", 5, 1, 20));

    private String[] cachedText = null;
    private AbstractSignEditScreen currentScreen = null;
    private int ticksWaited = 0;
    private boolean shouldFill = false;

    private boolean isReplacingPacket = false;  // stackoverflow lol

    public AutoSignModule() {
        super("AutoSign", "Automatically fills signs.", ModuleCategory.of("World"), "sign", "autosign");
    }

    @Override
    public void onDisable() {
        cachedText = null;
        currentScreen = null;
        ticksWaited = 0;
        shouldFill = false;
        isReplacingPacket = false;
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPacket() instanceof UpdateSignC2SPacket packet)) return;

        if (isReplacingPacket) {
            return;
        }

        if (cachedText == null) {
            cachedText = packet.getText();
            return;
        }

        if (shouldFill && currentScreen != null) {
            event.cancel();

            SignBlockEntity sign = ((SignEditScreenAccessor) currentScreen).getSign();

            isReplacingPacket = true;
            MC.player.networkHandler.sendPacket(new UpdateSignC2SPacket(
                    sign.getPos(),
                    packet.isFront(),
                    cachedText[0], cachedText[1], cachedText[2], cachedText[3]
            ));
            isReplacingPacket = false;

            shouldFill = false;
            currentScreen = null;
            ticksWaited = 0;
        }
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        if (event.getScreen() instanceof AbstractSignEditScreen screen && cachedText != null) {
            currentScreen = screen;
            ticksWaited = 0;
            shouldFill = true;
        }
    }

    // TODO its not working lol
    @SubscribeEvent
    public void onPreTick(PreTickEvent ev) {
        if (shouldFill) {
            ticksWaited++;
            if (ticksWaited >= delay.get()) {
                if (MC.currentScreen instanceof AbstractSignEditScreen) {
                    MC.setScreen(null);
                }
                shouldFill = false;
                currentScreen = null;
                ticksWaited = 0;
            }
        }
    }
}
