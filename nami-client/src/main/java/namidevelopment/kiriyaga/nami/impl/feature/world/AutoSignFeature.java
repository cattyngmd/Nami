package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.OpenScreenEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketSendEvent;
import namidevelopment.kiriyaga.api.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.nami.mixin.DuckSignEditScreen;
import namidevelopment.kiriyaga.api.model.setting.IntSetting;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static namidevelopment.kiriyaga.nami.Nami.MC;

@RegisterFeature
public class AutoSignFeature extends Feature {

    public final IntSetting delay = addSetting(new IntSetting("Delay", 5, 1, 20));
    public final BoolSetting timestamp = addSetting(new BoolSetting("Timestamp", false));

    private String[] cachedText = null;
    private AbstractSignEditScreen currentScreen = null;
    private int ticksWaited = 0;
    private boolean shouldFill = false;

    private boolean isReplacingPacket = false;  // stackoverflow lol

    public AutoSignFeature() {
        super("AutoSign", "Automatically fills signs.", FeatureCategory.of("World"), "sign", "autosign");
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
        if (!(event.getPacket() instanceof ServerboundSignUpdatePacket packet)) return;

        if (isReplacingPacket) {
            return;
        }

        if (cachedText == null) {
            cachedText = packet.getLines();
            return;
        }

        if (shouldFill && currentScreen != null) {
            event.cancel();

            SignBlockEntity sign = ((DuckSignEditScreen) currentScreen).getSign();

            String[] textToSend = cachedText.clone();
            if (timestamp.get()) {
                String date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
                textToSend[3] = date;
            }

            isReplacingPacket = true;
            MC.player.connection.send(new ServerboundSignUpdatePacket(sign.getBlockPos(), packet.isFrontText(), textToSend[0], textToSend[1], textToSend[2], textToSend[3]));
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

    @SubscribeEvent
    public void onPreTick(PreTickEvent ev) {
        if (shouldFill) {
            ticksWaited++;
            if (ticksWaited >= delay.get()) {
                if (MC.screen instanceof AbstractSignEditScreen) {
                    MC.setScreen(null);
                }
                shouldFill = false;
                currentScreen = null;
                ticksWaited = 0;
            }
        }
    }
}