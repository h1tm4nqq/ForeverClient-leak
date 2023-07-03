package we.devs.forever.client.ui.foreverClientGui.hud.component;


import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.hud.HUD;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

import java.awt.*;

public class ArrayListHud extends Hud {
    public static ArrayListHud INSTANCE;

    public Setting<Integer> animationHorizontalTime = (new Setting<>("AnimationHTime", 500, 1, 1000));
    public Setting<Integer> animationVerticalTime = (new Setting<>("AnimationVTime", 50, 1, 500));


    public ArrayListHud() {
        super("ArrayList");
        INSTANCE = this;
    }

    @Override
    public void onRenderHud() {
        width = 50.0F;
        height = (float) renderer.getHeight() * (moduleManager.sortedModules.size() + 1);
        int j = 0;
        int delay = 1;
        switch (anchor) {
            case TOP_LEFT:
            case BOTTOM_LEFT:
                for (Module module : moduleManager.sortedModules) {
                    String text = module.getName() + (module.getDisplayInfo() != null
                            ? TextUtil.GRAY + "[" + TextUtil.RESET + module.getDisplayInfo() + TextUtil.GRAY + "]"
                            : "");
                    renderer.drawStringWithShadow(text, X.getValue(), Y.getValue() + j * renderer.getHeight(), rainbow.getValue()
                            ? ColorUtil.rainbow(delay * rainbowSpeed.getValue()).getRGB()
                            : new Color(255, 255, 255, 255).getRGB());

                    delay++;
                    j++;

                }
                break;
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                int[] arrayOfInt = new int[]{1};
                for (int i = 0; i < moduleManager.sortedModules.size(); i++) {
                    Module module = moduleManager.sortedModules.get(i);
                    String text = module.getName() + (module.getDisplayInfo() != null ? TextUtil.GRAY + "[" + module.getDisplayInfo() + "]" : "");
                    renderer.drawStringWithShadow(text, X.getValue() + 50F - renderer.getStringWidth(text), Y.getValue() + j * 10, this.rainbow.getValue() ? ColorUtil.rainbow(arrayOfInt[0] * HUD.getInstance().rainbowSpeed.getValue()).getRGB() : new Color(255, 255, 255, 255).getRGB());
                    arrayOfInt[0] = arrayOfInt[0] + 1;
                    j++;
                }
                break;
        }

    }
}
