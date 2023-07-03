package we.devs.forever.client.modules.impl.combat.autocrystal;

import net.minecraft.entity.item.EntityEnderCrystal;
import we.devs.forever.api.event.events.entity.EntityWorldEvent;
import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerEntityWorldEvent extends ModuleListener<AutoCrystal, EntityWorldEvent.EntityRemoveEvent> {
    public ListenerEntityWorldEvent(AutoCrystal module) {
        super(module, EntityWorldEvent.EntityRemoveEvent.class);
    }

    @Override
    public void invoke(EntityWorldEvent.EntityRemoveEvent event) {
        // crystal being removed from world
        if (event.getEntity() instanceof EntityEnderCrystal) {

            // check if it is a crystal we have attacked
            if ( module.attackedCrystals.containsKey(event.getEntity().getEntityId())) {

                // remove crystal from our attacked crystals list
                module.lastConfirmTime =  System.currentTimeMillis() - module.attackedCrystals.remove(event.getEntity().getEntityId());

                // recently broke a crystal

            }

            // check if it is a crystal we have sent a packet for
            if (module.explosionPackets.contains(event.getEntity().getEntityId())) {

                // clear
                module.explosionPackets.clear();
            }

            module.inhibitCrystals.remove((EntityEnderCrystal) event.getEntity());
        }
    }
}
