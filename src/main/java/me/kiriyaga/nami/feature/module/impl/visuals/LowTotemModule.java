package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.HeldItemRendererEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class LowTotemModule extends Module {

    public final DoubleSetting amount = addSetting(new DoubleSetting("amount", 0.3, 0.1, 0.5));

    public LowTotemModule() {
        super("low totem", "Makes your shield/totem lower.", ModuleCategory.of("visuals"), "lowshield", "lowtotem");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onItemRenderer(HeldItemRendererEvent ev) {
        if (ev.getHand() != Hand.OFF_HAND) return;

        var item = MC.player.getOffHandStack().getItem();

        if (item == Items.TOTEM_OF_UNDYING || item == Items.SHIELD) {
            ev.getMatrix().translate(0, -amount.get(), 0);
        }
    }
}