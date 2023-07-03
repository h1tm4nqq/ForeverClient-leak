package we.devs.forever.api.event;


public
class EventStage {

    private int stage;
    private boolean Canceled;

    public EventStage() {
    }

    public EventStage(final int stage) {
        this.stage = stage;
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(final int stage) {
        this.stage = stage;
    }

    public boolean isCanceled() {
        return Canceled;
    }

    public void cancel() {
        Canceled = true;
    }

    public void setCanceled(boolean canceled) {
        Canceled = canceled;
    }
}
