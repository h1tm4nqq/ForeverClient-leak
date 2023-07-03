package we.devs.forever.client.modules.impl.client;

import org.lwjgl.input.Keyboard;
import we.devs.forever.client.modules.api.Module;

public class Friends extends Module {
    public  static Friends friends;
    public Friends() {
        super("Friends", "Attack friends if this has disabled", Category.CLIENT, false, true, Keyboard.KEY_NONE, true, KeyMode.Release, true, true);
        friends = this;
    }

}
