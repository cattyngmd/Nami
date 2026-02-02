package namidevelopment.kiriyaga.api.api.inventory;

import static namidevelopment.kiriyaga.api.NamiApi.*;

public class InventoryService {

    private final InventoryClickHandler clickHandler = new InventoryClickHandler();

    public void init() {
    }


    public InventoryClickHandler getClickHandler() {
        return clickHandler;
    }
}
