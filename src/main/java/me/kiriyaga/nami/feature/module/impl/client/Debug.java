package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.BoolSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class Debug extends Module {

    public final BoolSetting inventory = addSetting(new BoolSetting("inventory", false));
    public final BoolSetting auraGet = addSetting(new BoolSetting("aura", false));
    public final BoolSetting ping = addSetting(new BoolSetting("ping", false));
    public final BoolSetting shulkerSave = addSetting(new BoolSetting("shulkerSave", false));

    private Integer savedSyncId = null;

    public Debug() {
        super("debug", ".", ModuleCategory.of("client"), "debug");
    }

    @Override
    public void onEnable() {
        if (shulkerSave.get() && savedSyncId != null) {
            if (MC.player != null && MC.player.currentScreenHandler != null) {
                int currentSyncId = MC.player.currentScreenHandler.syncId;

                if (currentSyncId != savedSyncId) {
                    CHAT_MANAGER.sendRaw("No sync id");
                    toggle();
                } else {
                    CHAT_MANAGER.sendRaw("sync id matched: " + currentSyncId + " " + savedSyncId);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!shulkerSave.get()) return;

        if (event.getPacket() instanceof OpenScreenS2CPacket packet) {
            if (packet.getScreenHandlerType().equals(net.minecraft.screen.ScreenHandlerType.SHULKER_BOX)) {
                savedSyncId = packet.getSyncId();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onUpdate(PreTickEvent ev) {
        if (!shulkerSave.get()) return;

        Screen currentScreen = MC.currentScreen;
        if (currentScreen == null && isEnabled()) {
            toggle();
        }
    }
}
