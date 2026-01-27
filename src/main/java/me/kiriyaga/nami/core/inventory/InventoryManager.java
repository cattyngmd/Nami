package me.kiriyaga.nami.core.inventory;

import static me.kiriyaga.nami.Nami.LOGGER;

public class InventoryManager {

    private final InventorySlotHandler slotHandler = new InventorySlotHandler();
    private final InventoryClickHandler clickHandler = new InventoryClickHandler();

    public void init() {
        LOGGER.info("Inventory Manager loaded.");
    }

    public InventorySlotHandler getSlotHandler() {
        return slotHandler;
    }

    public InventoryClickHandler getClickHandler() {
        return clickHandler;
    }
}
