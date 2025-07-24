package me.kiriyaga.nami.core.inventory;

public class InventoryManager {

    private final InventorySlotHandler slotHandler = new InventorySlotHandler();
    private final InventoryClickHandler clickHandler = new InventoryClickHandler();
    private final InventorySyncHandler syncHandler = new InventorySyncHandler(slotHandler);

    public void init() {
        slotHandler.init();
        syncHandler.init();
    }

    public InventorySlotHandler getSlotHandler() {
        return slotHandler;
    }

    public InventoryClickHandler getClickHandler() {
        return clickHandler;
    }

    public InventorySyncHandler getSyncHandler() {
        return syncHandler;
    }
}
