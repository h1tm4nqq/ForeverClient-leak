package we.devs.forever.api.manager.impl.player;

import we.devs.forever.api.manager.api.AbstractManager;

public class InventoryManager extends AbstractManager {
    private boolean shouldWait;

    public InventoryManager() {
        super("InventoryManager");
    }
    public void moveInventory(Runnable runnable) {
        if(runnable != null){
            try {
                moveInventory0(runnable);
            } catch (InterruptedException ignored) {}
        }
    }

    private void moveInventory0(Runnable runnable) throws InterruptedException {
        synchronized (this) {
            while (shouldWait) wait();
            shouldWait = true;
        }
        runnable.run();
    }
    public  void finish(Runnable runnable) {
        runnable.run();
        finish();
    }
    public synchronized void finish() {
        shouldWait = false;
        notifyAll();
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}
