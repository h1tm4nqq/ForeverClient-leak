package we.devs.forever.client.modules.impl.client;

import org.lwjgl.input.Keyboard;
import we.devs.forever.client.modules.api.Module;

public class HudEditor extends Module {
    public static HudEditor INSTANCE;

    public HudEditor() {
        super("HudEditor", "Hud editor", Category.CLIENT, false,true, Keyboard.KEY_NONE, true,KeyMode.Release,true);
        INSTANCE = this;
    }

}
