package namidevelopment.kiriyaga.nami.impl.feature.world;

import namidevelopment.kiriyaga.nami.event.SubscribeEvent;
import namidevelopment.kiriyaga.nami.event.impl.PreTickEvent;
import namidevelopment.kiriyaga.nami.impl.feature.Feature;
import namidevelopment.kiriyaga.nami.impl.feature.FeatureCategory;
import namidevelopment.kiriyaga.nami.impl.feature.RegisterFeature;
import namidevelopment.kiriyaga.nami.impl.setting.impl.BoolSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.DoubleSetting;
import namidevelopment.kiriyaga.nami.impl.setting.impl.IntSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ItemStack;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.nami.util.InteractionUtils.interactWithEntity;

@RegisterFeature
public class AutoFrameFeature extends Feature {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 4, 1.0, 6.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 10, 0, 20));
    public final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    public final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));

    private int cooldown = 0;


    public AutoFrameFeature() {
        super("AutoFrame", "Automatically puts a map in nearby item frames.", FeatureCategory.of("World"), "autoframe");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.level == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        for (Entity entity : MC.level.entitiesForRendering()) {
            if (!(entity instanceof ItemFrame frame))
                continue;

            if (frame.getItem() != null)
                continue;

            int mapSlot = getMapSlot();
            if (mapSlot == -1)
                continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != mapSlot) {
                INVENTORY_SERVICE.getSlotHandler().attemptSwitch(mapSlot);
                cooldown = delay.get();
                return;
            }

            interactWithEntity(frame, range.get(), swing.get(), rotate.get(), this.name);

            cooldown = delay.get();
            break;
        }
    }

    private int getMapSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof MapItem) {
                return i;
            }
        }
        return -1;
    }
}