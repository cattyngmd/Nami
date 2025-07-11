package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.feature.module.Category;
import me.kiriyaga.nami.feature.module.Module;

public class NoJumpDelayModule extends Module {

    public NoJumpDelayModule() {
        super("no jump delay", "Removes vanilla jump delay which increases movement speed.", Category.movement, "nojumpdelay");
    }

}
