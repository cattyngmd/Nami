package me.kiriyaga.nami.feature.module.impl.client.cape;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.setting.impl.EnumSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;

@RegisterModule
public class CapeModule extends Module {
    public final EnumSetting<CapeType> cape = addSetting(new EnumSetting<>("texture", CapeType.LOST_CONNECTION));

    public CapeModule() {
        super("cape", "Defines cape renderer logic.", ModuleCategory.of("client"), "customcape");
    }

    public CapeType getSelectedCape() {
        return cape.get();
    }
}
