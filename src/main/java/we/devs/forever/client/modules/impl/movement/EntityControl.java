package we.devs.forever.client.modules.impl.movement;

import we.devs.forever.client.modules.api.Module;

public class EntityControl extends Module {
    public static EntityControl INSTANCE;

    public EntityControl() {
        super("EntityControl", "Control non saddled entities.", Category.MOVEMENT);
        EntityControl.INSTANCE = this;
    }

    public EntityControl getInstance() {
        return INSTANCE;
    }
}
