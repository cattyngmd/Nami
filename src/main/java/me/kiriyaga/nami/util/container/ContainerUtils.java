package me.kiriyaga.nami.util.container;

import me.kiriyaga.nami.feature.module.impl.client.Debug;
import net.fabricmc.fabric.mixin.transfer.ContainerComponentAccessor;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.Arrays;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class ContainerUtils {

    public static boolean hasItems(ItemStack stack) {
        ComponentMap components = stack.getComponents();
        return components.contains(DataComponentTypes.CONTAINER);
    }

    public static boolean openContainer(ItemStack stack) {
        if (!hasItems(stack)) {
            MODULE_MANAGER.getStorage().getByClass(Debug.class).debugPeek(Text.of("peek not a container " + stack));
            return false;
        }

        ItemStack[] contents = new ItemStack[27];
        getItemsInContainerItem(stack, contents);

        ContainerScreen.open(stack, contents);

        MODULE_MANAGER.getStorage().getByClass(Debug.class)
                .debugPeek(Text.of("peek opened container preview for " + stack));
        return true;
    }

    public static void getItemsInContainerItem(ItemStack itemStack, ItemStack[] items) {
        Arrays.fill(items, ItemStack.EMPTY);

        ComponentMap components = itemStack.getComponents();

        if (components.contains(DataComponentTypes.CONTAINER)) {
            ContainerComponentAccessor container = (ContainerComponentAccessor) (Object) components.get(DataComponentTypes.CONTAINER);
            DefaultedList<ItemStack> stacks = container.fabric_getStacks();

            for (int i = 0; i < stacks.size() && i < items.length; i++) {
                items[i] = stacks.get(i);
            }

            MODULE_MANAGER.getStorage().getByClass(Debug.class).debugPeek(Text.of("peek got " + stacks.size() + " items from container  " + itemStack));
        }
    }

}
