package we.devs.forever.api.event.events.player;

import net.minecraft.entity.Entity;
import we.devs.forever.api.event.EventStage;

public class ElytraEvent extends EventStage {


    private final Entity entity;

    public  ElytraEvent (Entity entity) {
        ///INSTANCE.setCanceled(false);
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}