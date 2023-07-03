package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.potion.PotionEffect;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.client.modules.impl.client.hud.HUD;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PotionsHud extends Hud {
    public PotionsHud() {
        super("Potions");
    }
    private int color;

    @Override
    public void onRenderHud() {
        width = 50.0F;
        height = (float) renderer.getHeight() * (potionManager.getOwnPotions().size() + 1);
        int i;
        float f;
        char[] stringToCharArray;
        int[] arrayOfInt;
        i = mc.currentScreen instanceof GuiChat ? 14 : 0;
        String text;
        List<String> effects = new ArrayList<>();
        for (PotionEffect effect : potionManager.getOwnPotions()) {
            text = potionManager.getPotionString(effect);
            effects.add(text);
        }
        Collections.sort(effects, Comparator.comparing(String::length));

        for (int x = effects.size() - 1; x >= 0; x--) {
            i += 10;
            text = effects.get(x);
            arrayOfInt = new int[]{1};
            f = 0.0f;
            stringToCharArray = text.toCharArray();
            for (char c : stringToCharArray) {
                this.renderer.drawStringWithShadow(String.valueOf(c), width - (renderer.getStringWidth(text)) + f - 2, height - (i), this.rainbow.getValue() ? ColorUtil.rainbow(arrayOfInt[0] * HUD.getInstance().rainbowSpeed.getValue()).getRGB() : color);
                f += (float) this.renderer.getStringWidth(String.valueOf(c));
                arrayOfInt[0] = arrayOfInt[0] + 1;
            }
        }
    }
}