package we.devs.forever.client.modules.impl.render;


import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class CrystalModify
        extends Module {
    public static CrystalModify INSTANCE;
    public Setting<modes> mode = (new Setting<>("Mode", modes.Fill));

    public Setting<Float> size = (new Setting<>("Size", 1.0f, 0.1f, 2.0f));
    public Setting<Float> crystalSpeed = (new Setting<>("Speed", 1.0f, 0.1f, 200.0f));
    public Setting<Float> crystalBounce = (new Setting<>("Bounce", 0.2f, 0.0f, 1.0f));
    public Setting<BlendModes> blendModes = (new Setting<>("Blend", BlendModes.Default));
    public Setting<Boolean> texture = (new Setting<>("Texture", false));

    public Setting<Color> color = (new Setting<>("Color", new Color(0, 60, 255, 150), ColorPickerButton.Mode.Normal, 100));
    public Setting<Boolean> enchanted = (new Setting<>("Glint", false));
    public Setting<Color> enchantColor = (new Setting<>("Glint Color", new Color(16, 77, 234, 171), ColorPickerButton.Mode.Normal, 100, v -> this.enchanted.getValue()));

    public Setting<Boolean> outline = (new Setting<>("Outline", false));
    public Setting<outlineModes> outlineMode = (new Setting<>("Outline Mode", outlineModes.Wire, v -> this.outline.getValue()));
    public Setting<Float> lineWidth = (new Setting<>("LineWidth", 1.0f, 0.1f, 5.0f, v -> this.outline.getValue()));
    public Setting<Color> outlineColor = (new Setting<>("Outline Color", new Color(221, 0, 255, 252), ColorPickerButton.Mode.Normal, 100, v -> this.outline.getValue()));

    public Setting<Boolean> hidden = (new Setting<>("Hidden", false));
    public Setting<Color> hiddenColor = (new Setting<>("Hidden  Color", new Color(3, 99, 236, 187), ColorPickerButton.Mode.Normal, 100, v -> this.hidden.getValue()));

    public CrystalModify() {
        super("CrystalChams", "Modifies crystal rendering in different ways", Category.RENDER);
        INSTANCE = this;
    }

    public enum modes {
        Fill,
        Wireframe
    }

    public enum outlineModes {
        Wire,
        Flat
    }

    public enum BlendModes {
        Default,
        Brighter
    }
}
