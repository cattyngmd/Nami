package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class InvCleanerModule extends Module {

    private final IntSetting delay = addSetting(new IntSetting("Delay", 1, 0, 10));
    private final WhitelistSetting whitelist = addSetting(new WhitelistSetting("Whitelist", false, WhitelistSetting.Type.ITEM));
    private final WhitelistSetting blacklist = addSetting(new WhitelistSetting("Blacklist", true, WhitelistSetting.Type.ITEM));

    private int i = 0;

    public InvCleanerModule() {
        super("InvCleaner", "Throws specified items from your inventory.", ModuleCategory.of("Miscellaneous"), "cleaner", "inventorycleaner");
    }

    @Override
    public void onDisable() {
        i = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;
        if (++i < delay.get()) return;
        i = 0;

        for (int i = 0; i < MC.player.getInventory().size(); i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            Identifier id = Registries.ITEM.getId(stack.getItem());

            if (blacklist.get() && blacklist.allows(WhitelistSetting.Type.ITEM)) {
                if (blacklist.isWhitelisted(id)) {
                    INVENTORY_MANAGER.getClickHandler().throwSlot(i);
                    return;
                }
            }

            if (whitelist.get() && whitelist.allows(WhitelistSetting.Type.ITEM)) {
                if (!whitelist.isWhitelisted(id)) {
                    INVENTORY_MANAGER.getClickHandler().throwSlot(i);
                    return;
                }
            }
        }
    }
}
