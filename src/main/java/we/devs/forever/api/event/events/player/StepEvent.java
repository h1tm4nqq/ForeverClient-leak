package we.devs.forever.api.event.events.player;

import net.minecraft.entity.Entity;
import we.devs.forever.api.event.EventStage;

public class StepEvent
extends EventStage {
    private final Entity entity;
    private float height;

    public StepEvent(int stage, Entity entity) {
        super(stage);
        this.entity = entity;
        this.height = entity.stepHeight;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}

