package we.devs.forever.api.util.player.inventory;

import net.minecraft.inventory.ClickType;
import we.devs.forever.client.Client;

public class Task extends Client {

    private final int slot;
    private final ClickType clickType;

    private final boolean update;

    public Task(int slot, ClickType clickType, boolean update) {
        this.slot = slot;
        this.clickType = clickType;
        this.update = update;
    }

    public Task() {
        this(-1, ClickType.PICKUP, true);
    }

    public Task(int slot, ClickType clickType) {
        this(slot, clickType, false);
    }


    public void run() {

        if (slot > 0 && slot < 9) {
            inventoryManager.moveInventory(() -> mc.playerController.windowClick(mc.player.inventoryContainer.windowId, this.slot, 0, clickType, mc.player));
            inventoryManager.finish();
        }
        if (update) mc.playerController.updateController();

    }

    public boolean isSwitching() {
        return !this.update;
    }

}
