package we.devs.forever.client.modules.impl.render;

import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class HandChams extends Module {
    public static HandChams INSTANCE;
    public Setting<Color> color = (new Setting<>("Color", new Color(32, 41, 203, 157), ColorPickerButton.Mode.Normal, 100));

    public HandChams() {
        super("HandChams", "Changes the color of your hands.", Category.RENDER);
        HandChams.INSTANCE = this;
    }
}
