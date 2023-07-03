package we.devs.forever.api.event.events.player;

import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.event.EventStage;

public
class DeathEvent extends EventStage {

    public EntityPlayer player;
    public int pops;

    public DeathEvent(EntityPlayer player, int pops) {
        this.player = player;
        this.pops = pops;
    }


}
