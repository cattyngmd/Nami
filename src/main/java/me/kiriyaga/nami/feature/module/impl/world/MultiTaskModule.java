
package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.InteractionEvent;
import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;

public class MultiTaskModule extends Module {

    public MultiTaskModule() {
        super("multi task", "Allows you using and breaking interaction at the same time.", Category.world, "multitask", "ьгдешефыл");
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    private void onInteractionEvent(InteractionEvent ev) {
        ev.cancel();
    }
}
