package we.devs.forever.api.util.combat;

import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.util.Util;


public class PredictPlayer implements Util {
    private final EntityPlayer oldPlayer;
    private final EntityPlayer target;

    public EntityPlayer getOldPlayer() {
        return oldPlayer;
    }

    public EntityPlayer getTarget() {
        return target;
    }

    public PredictPlayer(EntityPlayer oldPlayer, EntityPlayer target) {
        this.oldPlayer = oldPlayer;
        this.target = target;
    }


}
