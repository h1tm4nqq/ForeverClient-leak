package we.devs.forever.client.modules.impl.player;

import we.devs.forever.client.modules.api.Module;

public class LiquidInteract extends Module {
    private static LiquidInteract INSTANCE;

    static {
        LiquidInteract.INSTANCE = new LiquidInteract();
    }

    public LiquidInteract() {
        super("LiquidInteract", "Interact with liquids", Category.PLAYER);
        this.setInstance();
    }

    public static LiquidInteract getInstance() {
        if (LiquidInteract.INSTANCE == null) {
            LiquidInteract.INSTANCE = new LiquidInteract();
        }
        return LiquidInteract.INSTANCE;
    }

    private void setInstance() {
        LiquidInteract.INSTANCE = this;
    }
}
