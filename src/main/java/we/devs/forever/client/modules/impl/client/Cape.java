package we.devs.forever.client.modules.impl.client;

import org.lwjgl.input.Keyboard;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.newGui.api.Beta;

@Beta("SoonFix")
public class Cape extends Module {
    public  static Cape cape;
    public Cape() {
        super("Cape", "Render on you cape.", Category.CLIENT, false, true, Keyboard.KEY_NONE, true, KeyMode.Release, true, true);
        cape = this;
    }
    public final Setting<CapeMode> capeMode = (new Setting<>("CapeMode", CapeMode.Forever, "Your cape."));
    public final Setting<Image> image = (new Setting<>("CapeImage", new Image("NONE"), "Image on your cape."));

    public
    enum CapeMode {
        Forever,
        Custom,
        NONE
    }

}
