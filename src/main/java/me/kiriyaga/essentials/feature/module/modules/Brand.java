package me.kiriyaga.essentials.feature.module.modules;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.events.Render2DEvent;
import me.kiriyaga.essentials.event.events.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Module;

import static me.kiriyaga.essentials.Essentials.*;

public class Brand extends Module {
    public Brand() {
        super("Brand", "Draw brand logo", "logo");
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        CHAT_MANAGER.sendTransient("123123");
    }

    // for test only for now btw
    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {

        event.getDrawContext().drawText(MINECRAFT.textRenderer, NAME + VERSION, 5, 5, 0x55FFFF, true);
    }

}
