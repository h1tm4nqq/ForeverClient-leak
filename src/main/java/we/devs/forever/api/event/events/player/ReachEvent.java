package we.devs.forever.api.event.events.player;

import we.devs.forever.api.event.EventStage;

public class ReachEvent extends EventStage {

    public float distance;

    public ReachEvent(float distance) {
        this.distance = distance;
    }
}