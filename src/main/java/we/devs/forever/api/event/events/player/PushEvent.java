package we.devs.forever.api.event.events.player;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import we.devs.forever.api.event.EventStage;

@Cancelable
public
class PushEvent extends EventStage {

    public Entity entity;
    public double x;
    public double y;
    public double z;
    public boolean airbone;

    public PushEvent(Entity entity, double x, double y, double z, boolean airbone) {
        super(0);
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.airbone = airbone;
    }

    public PushEvent(int stage, double x, double y, double z) {
        super(stage);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PushEvent(int stage, Entity entity) {
        super(stage);
        this.entity = entity;
    }
}
