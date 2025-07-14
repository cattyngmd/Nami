package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.SprintResetEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import me.kiriyaga.nami.setting.impl.DoubleSetting;
import me.kiriyaga.nami.setting.impl.IntSetting;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.nami.Nami.MINECRAFT;

public class SprintResetModule extends Module {

    private final IntSetting chance = addSetting(new IntSetting("chance", 100, 0, 100));

    public SprintResetModule() {
        super("sprint reset", "Control how sprint resets after attacking.", Category.movement, "ызкштекуыуе");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSprintResetEvent(SprintResetEvent event) {
        if (!event.isCancelled()) {
            int random = (int) (Math.random() * 100);
            if (random < chance.get())
                event.cancel();
        }
    }
}
