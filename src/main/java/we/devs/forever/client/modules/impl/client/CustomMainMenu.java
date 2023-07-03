package we.devs.forever.client.modules.impl.client;

import org.lwjgl.input.Keyboard;
import we.devs.forever.client.modules.api.Module;

public class CustomMainMenu extends Module {
    public static CustomMainMenu INSTANCE;
    public CustomMainMenu() {
        super("Custom Main Menu", "Enable/disable custom main menu", Module.Category.CLIENT, false, true, Keyboard.KEY_NONE, true, KeyMode.Release, true, true);
        INSTANCE = this;
    }

//    @Override
//    public void onTick() {
//        if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) {
//            Minecraft.getMinecraft().displayGuiScreen(new GuiCustomMainScreen());
//        }
//    }
}
