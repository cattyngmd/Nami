package namidevelopment.kiriyaga.api.core.inventory;

public class InventoryService {

    private final InventoryClickHandler clickHandler = new InventoryClickHandler();

    public void init() {
    }


    public InventoryClickHandler getClickHandler() {
        return clickHandler;
    }
}
