package we.devs.forever.client.modules.impl.render;


import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class Chams extends Module {
    private static Chams INSTANCE;

    static {
        Chams.INSTANCE = new Chams();
    }

    public static final Setting<Boolean> fill = new Setting<>("Fill", true);
    public static final Setting<Color> color = new Setting<>("Color", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> fill.getValue());
    public static final Setting<Boolean> xqz = new Setting<>("XQZ", true);
    public static final Setting<Boolean> wireframe = new Setting<>("Wireframe", true);
    public static final Setting<Float> lineWidth = new Setting<>("LineWidth", 1.0f, 0.1f, 3.0f, v -> wireframe.getValue());
    public static final Setting<Color> lineColor = new Setting<>("LineColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> wireframe.getValue());
    public static final Setting<Model> model = new Setting<>("Model", Model.XQZ);
    public static final Setting<Color> modelColor = new Setting<>("ModelColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> model.getValue() != Model.None);
    public static final Setting<Boolean> glint = new Setting<>("Glint", false);
    public static final Setting<Color> glintColor = new Setting<>("GlintColor", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> glint.getValue());

    public static final Setting<Boolean> self = new Setting<>("Self", true);
    public static final Setting<Boolean> noInterp = new Setting<>("NoInterp", false);
    public static final Setting<Boolean> sneak = new Setting<>("Sneak", false);



    public Chams() {
        super("Chams", "Renders players through walls.", Category.RENDER);

        this.setInstance();
    }

    public static Chams getInstance() {
        if (Chams.INSTANCE == null) {
            Chams.INSTANCE = new Chams();
        }
        return Chams.INSTANCE;
    }

    private void setInstance() {
        Chams.INSTANCE = this;
    }

    public enum Model {
        XQZ,
        Vanilla,
        None
    }


}
