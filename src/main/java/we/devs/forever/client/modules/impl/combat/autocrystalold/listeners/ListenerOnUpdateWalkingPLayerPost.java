package we.devs.forever.client.modules.impl.combat.autocrystalold.listeners;

import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;

public class ListenerOnUpdateWalkingPLayerPost extends ModuleListener<AutoCrystal, MotionEvent.Post> {
    public ListenerOnUpdateWalkingPLayerPost(AutoCrystal module) {
        super(module,  MotionEvent.Post.class);
    }

    @Override
    public void invoke(MotionEvent.Post event) {
//        module.doCrystal(false);
    }
}