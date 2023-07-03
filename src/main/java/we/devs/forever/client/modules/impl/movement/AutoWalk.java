package we.devs.forever.client.modules.impl.movement;

import net.minecraftforge.client.event.InputUpdateEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", "Automatically walks in a straight line", Category.MOVEMENT);
    }

    @EventListener
    public void onUpdateInput(final InputUpdateEvent event) {
        event.getMovementInput().moveForward = 1.0f;
    }
}
