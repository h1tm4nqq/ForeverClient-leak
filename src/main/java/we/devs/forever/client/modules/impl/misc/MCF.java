package we.devs.forever.client.modules.impl.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import we.devs.forever.api.event.events.client.KeyEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;

public class MCF extends Module {
    public MCF() {
        super("MCF", "MiddleClick Friends.", Category.MISC);
    }

    @EventListener
    public void onMouse(KeyEvent event) {
        if(event.iskeyDown() && event.getKey() == mc.gameSettings.keyBindPickBlock.getKeyCode()) {
            Entity entity;
            RayTraceResult result = MCF.mc.objectMouseOver;
            if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {
                if(friendManager.isFriend(entity.getName())) {
                    friendManager.removeFriend(entity.getName());
                    Command.sendMessage("Remove " + entity.getName() + " from friend list.");
                } else {
                    friendManager.addFriend(entity.getName());
                    Command.sendMessage("Add " + entity.getName() + " to friend list.");
                }
            }
        }
    }
}
