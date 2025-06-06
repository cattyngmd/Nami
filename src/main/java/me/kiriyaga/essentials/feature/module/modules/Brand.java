package me.kiriyaga.essentials.feature.module.modules;

import me.kiriyaga.essentials.event.SubscribeEvent;
import me.kiriyaga.essentials.event.events.Render2DEvent;
import me.kiriyaga.essentials.event.events.UpdateEvent;
import me.kiriyaga.essentials.feature.module.Module;
import me.kiriyaga.essentials.feature.setting.settings.BoolSetting;
import me.kiriyaga.essentials.feature.setting.settings.DoubleSetting;
import me.kiriyaga.essentials.feature.setting.settings.EnumSetting;

import static me.kiriyaga.essentials.Essentials.*;

public class Brand extends Module {

    public final DoubleSetting first = addSetting(new DoubleSetting("range", 1.5, 1.0, 5.0));
    public final BoolSetting second = addSetting(new BoolSetting("anticheat exploit hack popbob", true));
    public final EnumSetting<Brand.Mode> third = addSetting(new EnumSetting<>("sex mode erp", Mode.first));

    public Brand() {
        super("Brand", "Draw brand logo", "logo");
    }

    public enum Mode {
        third, second, first
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
       // CHAT_MANAGER.sendTransient("123123");
    }

    // for test only for now btw
    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (MINECRAFT == null || MINECRAFT.inGameHud == null) return;

        event.getDrawContext().drawText(MINECRAFT.textRenderer, NAME + " " + VERSION, 5, 5, 0x55FFFF, true);
    }

}
