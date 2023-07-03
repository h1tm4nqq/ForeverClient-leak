package we.devs.forever.api.event.events.render;

import we.devs.forever.api.event.EventStage;

public class GetWorldTimeEvent extends EventStage {

    private long worldTime = 6000;

    public long getWorldTime() {
        return worldTime;
    }

    public void setWorldTime(long worldTime) {
        this.worldTime = worldTime;
    }
}
