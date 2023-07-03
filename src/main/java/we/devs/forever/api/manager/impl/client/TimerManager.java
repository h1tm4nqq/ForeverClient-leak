package we.devs.forever.api.manager.impl.client;

import we.devs.forever.api.manager.api.AbstractManager;

public
class TimerManager extends AbstractManager {
    private final boolean active = false;
    private float timer = 1.0f;
    private boolean tpsSync = false;
   public int priority = 0;

    public TimerManager() {
        super("TimerManager");
    }


    public boolean isTpsSync() {
        return tpsSync;
    }

    public void setTpsSync(boolean tpsSync) {
        this.tpsSync = tpsSync;
    }

    public float getTimer() {
        return this.timer;
    }

    public void setTimer(float timer, int priority) {
        if (timer > 0.0f && priority > this.priority) {
            this.timer = timer;
            this.priority =  priority;
        }
    }

    public void reset(int priority) {
        timer = 1.0f;
        this.priority =  priority;
    }

    @Override
    protected void onLoad() {
    }

    @Override
    protected void onUnload() {

    }
}
