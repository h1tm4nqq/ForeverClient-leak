package we.devs.forever.api.event.events.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import we.devs.forever.api.event.EventStage;

@Cancelable
public class EntityWorldEvent extends EventStage {

    private final Entity entity;

    public EntityWorldEvent(Entity entity) {
        this.entity = entity;
    }

    public static class EntitySpawnEvent extends EntityWorldEvent {
        public EntitySpawnEvent(Entity entity) {
            super(entity);
        }
    }

    public static class EntityRemoveEvent extends EntityWorldEvent {
        public EntityRemoveEvent(Entity entity) {
            super(entity);
        }
    }

    public static class EntityUpdateEvent extends EntityWorldEvent {
        public EntityUpdateEvent(Entity entity) {
            super(entity);
        }
    }

    public Entity getEntity() {
        return entity;
    }
}
