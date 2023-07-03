package we.devs.forever.client.modules.impl.client;

import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.client.ui.newGui.api.Beta;

import java.awt.*;

@Beta("ForTest")
public class Config1 extends Module {
    //  public Image cape = new Image("C:\\Program Files (x86)\\кубы\\instances\\1.12.2\\.minecraft\\forever\\test.gif");
    public static Config1 INSTANCE;
    public static Setting<Float> X = (new Setting<>("X", 1.0F, -20F, 20F));
    public static Setting<Float> Y = (new Setting<>("Y", 1.0F, -20F, 20F));
    public static Setting<Float> X1 = (new Setting<>("X1", 5.0F, -2000F, 2000F));
    public static Setting<Float> Y1 = (new Setting<>("Y1", 5.0F, -2000F, 2000F));
    public static Setting<Float> X2 = (new Setting<>("X2", 10.0F, 0.0F, 20.0F));
    public static Setting<Color> color = (new Setting<>("Color", Color.RED, ColorPickerButton.Mode.Normal, 100));
    public static Setting<Float> Y2 = (new Setting<>("Y2", 10.0F, 0.0F, 140.0F));
    public static Setting<Float> X3 = (new Setting<>("X3", 10.0F, 0.0F, 140.0F));
    public static Setting<Integer> X4 = (new Setting<>("X4", 10, 1, 200));
    //   public Setting<Float> speedCape = (new Setting<>("speedCape", 15F, 1F, 15F));

    //    public Setting<Float> X = (new Setting<>("X", 10.0F, 0F, 1000.0F));
//    public Setting<Float> Y = (new Setting<>("Y", 10.0F, 0F, 1000.0F));
//    public Setting<Float> X2 = (new Setting<>("X2", 10.0F, 0F, 1000.0F));
//    public Setting<Float> Y2 = (new Setting<>("Y2", 10.0F, 0F, 1000.0F));
    public static Setting<Float> with = (new Setting<>("With", 10.0F, 0F, 20.0F));
    public static Setting<Boolean> add = (new Setting<>("AddNotify", false));
    public Setting<Boolean> debug = (new Setting<>("Debug", false));
    public static Setting<Boolean> debug1 = (new Setting<>("Debug1", false));

    public Config1() {
        super("Config", "Fuck you", Category.CLIENT);
        INSTANCE = this;
    }


//    @Override
//    public void onEnable() {
//        cape = new Image(speedCape.getValue());
//    }

    @Override
    public void onTick() {
        if (add.getValue()) {
            notificationManager.addNotification("Test ", 10000);
        }
    }

}