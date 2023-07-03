package we.devs.forever.api.event.events.player;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import we.devs.forever.api.event.EventStage;

@Cancelable
public class TurnEvent extends EventStage {
    private final float yaw;
    private final float pitch;

    public TurnEvent(final float yaw, final float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}