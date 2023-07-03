package we.devs.forever.api.event.events.player;

import we.devs.forever.api.event.EventStage;

public class TravelEvent extends EventStage {
    public float Strafe;
    public float Vertical;
    public float Forward;

    public TravelEvent(float p_Strafe, float p_Vertical, float p_Forward) {
        Strafe = p_Strafe;
        Vertical = p_Vertical;
        Forward = p_Forward;
    }
}