package we.devs.forever.api.event.events.player;

import we.devs.forever.api.event.EventStage;

public class StopUsingItemEvent extends EventStage {
    private boolean packet = false;

    public boolean isPacket() {
        return packet;
    }

    public void setPacket(boolean packet) {
        this.packet = packet;
    }
}
