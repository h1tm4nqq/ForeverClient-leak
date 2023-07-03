package we.devs.forever.client.modules.impl.render;

import we.devs.forever.api.event.events.render.RenderItemEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class ViewModel extends Module {
    private static ViewModel INSTANCE;

    static {
        ViewModel.INSTANCE = new ViewModel();
    }

    public Setting<Settings> settings = (new Setting<>("Settings", Settings.Hands));
    public Setting<Double> mainX = (new Setting<>("MainX", 1.3, -2.0, 4.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainY = (new Setting<>("MainY", -0.5, -3.0, 3.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainZ = (new Setting<>("MainZ", -1.45, -5.0, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offX = (new Setting<>("OffX", 1.3, -2.0, 4.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offY = (new Setting<>("OffY", -0.5, -3.0, 3.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offZ = (new Setting<>("OffZ", -1.45, -5.0, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainRotX = (new Setting<>("MainRotationX", 0.0, -180.0, 180.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainRotY = (new Setting<>("MainRotationY", 0.0, -180.0, 180.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainRotZ = (new Setting<>("MainRotationZ", 0.0, -180.0, 180.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offRotX = (new Setting<>("OffRotationX", 0.0, -180.0, 180.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offRotY = (new Setting<>("OffRotationY", 0.0, -180.0, 180.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offRotZ = (new Setting<>("OffRotationZ", 0.0, -180.0, 180.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainScaleX = (new Setting<>("MainScaleX", 1.0, 0.1, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainScaleY = (new Setting<>("MainScaleY", 1.0, 0.1, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> mainScaleZ = (new Setting<>("MainScaleZ", 1.0, 0.1, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offScaleX = (new Setting<>("OffScaleX", 1.0, 0.1, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offScaleY = (new Setting<>("OffScaleY", 1.0, 0.1, 5.0, v -> settings.getValue() == Settings.Hands));
    public Setting<Double> offScaleZ = (new Setting<>("OffScaleZ", 1.0, 0.1, 5.0, v -> settings.getValue() == Settings.Hands));
    //EAT
    public Setting<Mode> mode = (new Setting<>("ModeEat", Mode.Custom, v -> settings.getValue() == ViewModel.Settings.Eat));
    public Setting<Float> eat = (new Setting<>("Eat", 1.0F, 0.0F, 2.0F, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    public Setting<Double> mainXEat = (new Setting<>("MainXEat", 6.0, -8.0, 8.0, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    public Setting<Double> mainYEat = (new Setting<>("MainYEat", -2.9, -8.00, 8.0, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    public Setting<Double> mainZEat = (new Setting<>("MainZEat", 0.0, -8.0, 8.0, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    public Setting<Double> offXEat = (new Setting<>("OffXEat", -6.0, -8.0, 8.0, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    public Setting<Double> offYEat = (new Setting<>("OffYEat", -2.9, -8.0, 8.0, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    public Setting<Double> offZEat = (new Setting<>("OffZEat", 0.0, -8.0, 8.0, v -> settings.getValue() == ViewModel.Settings.Eat && mode.getValue() == Mode.Custom));
    //    //TWEAKS
    public Setting<Boolean> doBob = (new Setting<>("ItemBob", true, v -> settings.getValue() == Settings.Tweaks));
    public Setting<Boolean> noSway = (new Setting<>("NoSway", false, v -> settings.getValue() == Settings.Tweaks));
    public Setting<Boolean> colors = (new Setting<>("Colors", false, v -> settings.getValue() == Settings.Tweaks));
    public Setting<Float> red = (new Setting<>("Red", 255f, 0f, 255f, v -> settings.getValue() == Settings.Tweaks && colors.getValue()));
    public Setting<Float> green = (new Setting<>("Green", 255f, 0f, 255f, v -> settings.getValue() == Settings.Tweaks && colors.getValue()));
    public Setting<Float> blue = (new Setting<>("Blue", 255f, 0f, 255f, v -> settings.getValue() == Settings.Tweaks && colors.getValue()));
    public Setting<Float> alpha = (new Setting<>("Alpha", 255f, 0f, 255f, v -> settings.getValue() == Settings.Tweaks && colors.getValue()));


    //GlintModify
    public Setting<Boolean> glintModify = (new Setting<>("GlintModify", false, v -> settings.getValue() == Settings.GlintModify));
    public Setting<Color> glintModifyColor = (new Setting<>("GlintModifyColor", new Color(2, 49, 155, 255), ColorPickerButton.Mode.Normal, 100, v -> settings.getValue() == Settings.GlintModify && glintModify.getValue()));

    public ViewModel() {
        super("ViewModel", "Change the looks of items in 1st player view.", Category.RENDER);

        setInstance();
    }

    public static ViewModel getInstance() {
        if (ViewModel.INSTANCE == null) {
            ViewModel.INSTANCE = new ViewModel();
        }
        return ViewModel.INSTANCE;
    }



    private void setInstance() {
        ViewModel.INSTANCE = this;
    }

    @EventListener
    public void onItemRender(final RenderItemEvent event) {
        event.setMainX(mainX.getValue());
        event.setMainY(mainY.getValue());
        event.setMainZ(mainZ.getValue());
        event.setOffX(-offX.getValue());
        event.setOffY(offY.getValue());
        event.setOffZ(offZ.getValue());
        event.setMainRotX(mainRotX.getValue());
        event.setMainRotY(mainRotY.getValue());
        event.setMainRotZ(mainRotZ.getValue());
        event.setOffRotX(offRotX.getValue());
        event.setOffRotY(offRotY.getValue());
        event.setOffRotZ(offRotZ.getValue());
        event.setOffHandScaleX(offScaleX.getValue());
        event.setOffHandScaleY(offScaleY.getValue());
        event.setOffHandScaleZ(offScaleZ.getValue());
        event.setMainHandScaleX(mainScaleX.getValue());
        event.setMainHandScaleY(mainScaleY.getValue());
        event.setMainHandScaleZ(mainScaleZ.getValue());
    }

    public enum Mode {
        Normal,
        Cancel,
        Custom
    }

    private enum Settings {
        Hands,
        Eat,
        Tweaks,
        GlintModify
    }
}
