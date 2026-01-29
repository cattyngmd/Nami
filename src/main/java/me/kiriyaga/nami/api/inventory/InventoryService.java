package me.kiriyaga.nami.api.inventory;

import static me.kiriyaga.nami.Nami.LOGGER;

public class InventoryService {

    private final InventorySlotHandler slotHandler = new InventorySlotHandler();
    private final InventoryClickHandler clickHandler = new InventoryClickHandler();

    public void init() {
        LOGGER.info("Inventory SERVICE loaded.");
    }

    public InventorySlotHandler getSlotHandler() {
        return slotHandler;
    }

    public InventoryClickHandler getClickHandler() {
        return clickHandler;
    }
}
