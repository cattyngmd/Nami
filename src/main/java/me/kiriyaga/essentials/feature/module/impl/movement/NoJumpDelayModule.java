package me.kiriyaga.essentials.feature.module.impl.movement;

import me.kiriyaga.essentials.feature.module.Category;
import me.kiriyaga.essentials.feature.module.Module;

public class NoJumpDelayModule extends Module {

    public NoJumpDelayModule() {
        super("no jump delay", "Removes vanilla jump delay which increases movement speed.", Category.movement, "nojumpdelay");
    }

}
