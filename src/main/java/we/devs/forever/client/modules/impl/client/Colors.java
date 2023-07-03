package we.devs.forever.client.modules.impl.client;


import org.lwjgl.input.Keyboard;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class Colors extends Module {
    public static Colors INSTANCE;
    public Setting<Color> colorSetting = new Setting<>("   ",Color.WHITE, ColorPickerButton.Mode.Normal,100,true);


    public Colors() {
        super("Colors", "Universal colors.", Category.CLIENT, false, true, Keyboard.KEY_NONE, true, KeyMode.Release, true, true);
        Colors.INSTANCE = this;
    }

    @Override
    public void onLoad() throws Throwable {
        visible.setValue(false);
    }

    public static Colors getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Colors();
        }
        return INSTANCE;
    }

    public int getCurrentColorHex() {
        return colorSetting.getColor().getRGB();
    }

    public Color getCurrentColor() {
        return colorSetting.getColor();
    }
}