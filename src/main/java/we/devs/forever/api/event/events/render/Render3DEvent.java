package we.devs.forever.api.event.events.render;

import we.devs.forever.api.event.EventStage;
@Deprecated //TODO выпилить нахуй это говно
public class Render3DEvent extends EventStage {

    private final float partialTicks;

    public Render3DEvent(final float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}
