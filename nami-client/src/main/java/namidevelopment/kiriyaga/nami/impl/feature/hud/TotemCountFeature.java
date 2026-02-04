package namidevelopment.kiriyaga.nami.impl.feature.hud;

import namidevelopment.kiriyaga.api.model.feature.HudElementFeature;
import namidevelopment.kiriyaga.api.annotation.RegisterFeature;
import namidevelopment.kiriyaga.api.model.setting.BoolSetting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

import java.util.List;

import static namidevelopment.kiriyaga.nami.Nami.*;
import static namidevelopment.kiriyaga.api.NamiApi.*;
@RegisterFeature
public class TotemCountFeature extends HudElementFeature {

    public final BoolSetting white = addSetting(new BoolSetting("White", true));

    public TotemCountFeature() {
        super("TotemCount", "Displays number of totems in inventory.", 0, 0, 20, 20);
        this.label.setShow(true);
    }

    private int countTotems() {
        int count = 0;

        if (MC.player == null) return 0;

        for (ItemStack stack : MC.player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }

        ItemStack offhand = MC.player.getOffhandItem();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            count += offhand.getCount();
        }

        return count;
    }

    @Override
    public List<LabeledItemElement> getLabeledItemElements() {
        int totemCount = countTotems();
        ItemStack totemStack = new ItemStack(Items.TOTEM_OF_UNDYING);
        Component label;

        if (white.get())
            label = CAT_FORMAT.format("{white}"+totemCount);
        else
            label = CAT_FORMAT.format("{global}"+totemCount);

        width = 16;
        height = 16;
        return List.of(new LabeledItemElement(totemStack, label, this.label.get(), 0, 0, 1));
    }
}