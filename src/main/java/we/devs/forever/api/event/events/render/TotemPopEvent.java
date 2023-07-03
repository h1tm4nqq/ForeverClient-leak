package we.devs.forever.api.event.events.render;

import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.event.EventStage;

public
class TotemPopEvent extends EventStage {

    private final EntityPlayer entity;
    private final int pops;

    public TotemPopEvent(EntityPlayer entity, int pops) {
        super();
        this.entity = entity;
        this.pops = pops;
    }

    public EntityPlayer getEntity() {
        return entity;
    }


    public int getPops() {
        return pops;
    }
}
