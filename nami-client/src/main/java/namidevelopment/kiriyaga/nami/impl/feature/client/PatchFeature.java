package namidevelopment.kiriyaga.nami.impl.feature.client;

import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.event.EventPriority;
import namidevelopment.kiriyaga.api.annotation.SubscribeEvent;
import namidevelopment.kiriyaga.api.event.impl.PacketReceiveEvent;
import namidevelopment.kiriyaga.api.event.impl.SprintResetEvent;
import namidevelopment.kiriyaga.api.model.feature.Feature;
import namidevelopment.kiriyaga.api.model.feature.FeatureCategory;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;

import static namidevelopment.kiriyaga.api.NamiApi.API_MC;
import static namidevelopment.kiriyaga.api.NamiApi.CHAT_SERVICE;


@RegisterFeature
public class PatchFeature extends Feature {

    public final BoolSetting grimAttackVelocity = addSetting(new BoolSetting("GrimAttackVelocity", true));
    public final BoolSetting slotDragDesync = addSetting(new BoolSetting("SlotDragDesync", true));
    public final BoolSetting silentSwapFix = addSetting(new BoolSetting("SilentSwapFix", true));
    public final BoolSetting setSlotDebug = addSetting(new BoolSetting("SetSlotDebug", false));

    public PatchFeature() {
        super("Patch", "Any kind of hotfixes you should apply based on what server and ac u on.", FeatureCategory.of("Client"));
        if (!this.isEnabled())
            this.toggle();
        setSlotDebug.setShow(false);
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSprintResetEvent(SprintResetEvent event) {
        if (grimAttackVelocity.get() && !event.isCancelled())
                event.cancel();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        Packet<?> p = event.getPacket();

        if (silentSwapFix.get() && p instanceof ClientboundContainerSetSlotPacket packet) {
            if (API_MC.player != null) {
                if (setSlotDebug.get()) {
                API_MC.execute(() -> {
                    CHAT_SERVICE.sendPersistent("ContainerID: ", "ContainerID: " +packet.getContainerId()+"");
                    CHAT_SERVICE.sendPersistent("StateID: ", "StateID: " +packet.getStateId()+"");
                    CHAT_SERVICE.sendPersistent("Item: ", "Item: " +packet.getItem()+"");
                    CHAT_SERVICE.sendPersistent("Slot: ", "Slot: " +packet.getSlot()+"");
                    CHAT_SERVICE.sendPersistent("Type: ", "Type: " +packet.type()+"");

                });

                }
                if (packet.getContainerId() == 0) { // only player inventory, syncid of player inventory is always 0
                    int slot = packet.getSlot();

                    if (slot >= 36 && slot <= 44) { // onlu hotbar
                        ItemStack packetStack = packet.getItem();
                        ItemStack handStack = API_MC.player.getMainHandItem();

                        if (!packetStack.isEmpty() && !handStack.isEmpty()) {
                            if (ItemStack.isSameItem(packetStack, handStack) && packetStack.getCount() == handStack.getCount()) {
                                if (setSlotDebug.get()) {

                                    API_MC.execute(() -> {
                                        CHAT_SERVICE.sendPersistent("1", "canceled yo");
                                    });
                                }
                                event.cancel(); // TODO: maybe delay it to 2 ticks instead of canceling, like in mio
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
