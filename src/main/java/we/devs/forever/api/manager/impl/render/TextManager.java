package we.devs.forever.api.manager.impl.render;

import net.minecraft.util.math.MathHelper;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.impl.client.FontModule;
import we.devs.forever.client.ui.Fonts.font.FontRenderer;

import java.awt.*;

public
class TextManager extends AbstractManager {
    private final TimerUtil idleTimer = new TimerUtil();
    public int scaledWidth;
    public int scaledHeight;
    public int scaleFactor;
    private FontRenderer customFont = new FontRenderer(new Font("/assets/forever/fonts/RobotoFlex.ttf", Font.PLAIN, 17));
    private boolean idling;

    public final String syncCode = "§(";

    public FontRenderer getCustomFont() {
        return customFont;
    }

    public TextManager() {
        super("TextManager");
        updateResolution();
    }

    public void init() {

//        if (FontMod.INSTANCE == null) {
//            FontMod.INSTANCE = new FontMod();
//        }
//
//        FontMod fonts = FontMod.INSTANCE;
//

    }

    public String normalizeCases(Object o) {
        return Character.toUpperCase(o.toString().charAt(0)) + o.toString().toLowerCase().substring(1);
    }

    public float drawStringNoCFont(String text, float x, float y, int color, boolean shadow) {
        mc.fontRenderer.drawString(text, x, y, color, shadow);
        return x;
    }

    public void drawStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x, y, color, true, true);
    }
    public void drawStringWithShadow(String text, float x, float y, int color, boolean custom) {
        drawString(text, x, y, color, true, custom);
    }
    public float drawString(String text, float x, float y, int color, boolean shadow) {
        if(FontModule.fontModule.isEnabled()) {
            return drawString(text, x, y, color, shadow, true);
        } else {
         return  mc.fontRenderer.drawString(text, x, y, color, shadow);
        }

    }
    public float drawString(String text, float x, float y, int color, boolean shadow, boolean custom) {
        if (FontModule.fontModule.isEnabled()) {
            if (shadow) {
                customFont.drawStringWithShadow(text, x, y, color);

            } else {
                customFont.drawString(text, x, y, color);
            }
            return x;
        }
        mc.fontRenderer.drawString(text, x, y, color, shadow);

        return x;
    }

    public void drawRollingRainbowString(String text, float x, float y, boolean shadow) {
//        Pattern.compile("(?i)\u00a7[0-9A-FK-OR]").matcher(text).replaceAll("");
//        int[] arrayOfInt = {1};
//        char[] stringToCharArray = (text).toCharArray();
//        float f = 0.0f + x;
//        for (char c : stringToCharArray) {
//            drawString(String.valueOf(c), f,
//                    y, ColorUtil.rainbow(arrayOfInt[0] * (ClickGui.INSTANCE).rainbowDelay.getValue()).getRGB(), shadow);
//            f += getStringWidth(String.valueOf(c));
//            arrayOfInt[0] = arrayOfInt[0] + 1;
//        }
    }


    public int getStringWidth(String text) {
        if (FontModule.fontModule.isEnabled()) {
            return  customFont.getStringWidth(text);
        }
        return mc.fontRenderer.getStringWidth(text);
    }
    public int getHeight() {
        return getFontHeight();
    }
    public int getFontHeight() {
        if (FontModule.fontModule.isEnabled()) {
            return (int) customFont.getHeight();
        }
        return mc.fontRenderer.FONT_HEIGHT;
    }

    public void setFontRenderer(String s,int style, int scale) {
        customFont = new FontRenderer(new Font(s, style,scale));
    }



    public void updateResolution() {
        scaledWidth = mc.displayWidth;
        scaledHeight = mc.displayHeight;
        scaleFactor = 1;
        boolean flag = mc.isUnicode();

        int i = mc.gameSettings.guiScale;

        if (i == 0) {
            i = 1000;
        }

        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) {
            --scaleFactor;
        }

        double scaledWidthD = scaledWidth / scaleFactor;
        double scaledHeightD = scaledHeight / scaleFactor;

        scaledWidth = MathHelper.ceil(scaledWidthD);
        scaledHeight = MathHelper.ceil(scaledHeightD);
    }

    public String getIdleSign() {
        if (idleTimer.passedMs(500L)) {
            idling = !idling;
            idleTimer.reset();
        }
        if (idling) {
            return "_";
        }
        return "";
    }

    @Override
    protected void onLoad() {
        try {
            setFontRenderer(FontModule.fontModule.modef.getValue().value,FontModule.fontModule.mode.getValue().value,FontModule.fontModule.fontscale.getValue());
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onUnload() {

    }
}
