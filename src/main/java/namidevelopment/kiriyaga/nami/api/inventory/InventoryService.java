package namidevelopment.kiriyaga.nami.api.inventory;

import static namidevelopment.kiriyaga.nami.Nami.LOGGER;

public class InventoryService {

    private final InventorySlotHandler slotHandler = new InventorySlotHandler();
    private final InventoryClickHandler clickHandler = new InventoryClickHandler();

    public void init() {
    }

    public InventorySlotHandler getSlotHandler() {
        return slotHandler;
    }

    public InventoryClickHandler getClickHandler() {
        return clickHandler;
    }
}
