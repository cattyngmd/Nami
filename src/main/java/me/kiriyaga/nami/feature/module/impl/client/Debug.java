package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.CHAT_MANAGER;

@RegisterModule
public class Debug extends Module {

    private final BoolSetting aura = addSetting(new BoolSetting("aura", false));
    private final BoolSetting ping = addSetting(new BoolSetting("ping", false));
    private final BoolSetting speedrot = addSetting(new BoolSetting("speedrot", false));

    private Integer savedSyncId = null;

    public Debug() {
        super("debug", ".", ModuleCategory.of("client"), "debug");
    }

    public boolean debugAura(Text text){
        if (this.isEnabled() && aura.get()) {
            CHAT_MANAGER.sendRaw(text);
            return true;
        }
        return false;
    }

    public boolean debugPing(Text text){
        if (this.isEnabled() && ping.get()) {
            CHAT_MANAGER.sendRaw(text);
            return true;
        }
        return false;
    }

    public boolean debugSpeedRot(Text text){
        if (this.isEnabled() && speedrot.get()) {
            CHAT_MANAGER.sendRaw(text);
            return true;
        }
        return false;
    }


//    @Override
//    public void onEnable() {
//        if (shulkerSave.get() && savedSyncId != null) {
//            if (MC.player != null && MC.player.currentScreenHandler != null) {
//                int currentSyncId = MC.player.currentScreenHandler.syncId;
//
//                if (currentSyncId != savedSyncId) {
//                    CHAT_MANAGER.sendRaw("No sync id");
//                    toggle();
//                } else {
//                    CHAT_MANAGER.sendRaw("sync id matched: " + currentSyncId + " " + savedSyncId);
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public void onPacketReceive(PacketReceiveEvent event) {
//        if (!shulkerSave.get()) return;
//
//        if (event.getPacket() instanceof OpenScreenS2CPacket packet) {
//            if (packet.getScreenHandlerType().equals(net.minecraft.screen.ScreenHandlerType.SHULKER_BOX)) {
//                savedSyncId = packet.getSyncId();
//            }
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    private void onUpdate(PreTickEvent ev) {
//        if (!shulkerSave.get()) return;
//
//        Screen currentScreen = MC.currentScreen;
//        if (currentScreen == null && isEnabled()) {
//            toggle();
//        }
//    }
}
