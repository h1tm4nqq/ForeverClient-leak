package we.devs.forever.client.modules.impl.client.hud;

import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;

public class CSGOWatermark extends HUD {


    public float hue;
    public int red = 1;
    public int green = 1;
    public int blue = 1;
    TimerUtil delayTimer = new TimerUtil();
    private String message = "";

    public void drawCsgoWatermark() {
        int padding = 5;
        message = "ForeverClient v0.1.9 | " + mc.player.getName() + " | " + serverManager.getPing() + "ms";
        Integer textWidth = mc.fontRenderer.getStringWidth(message);
        Integer textHeight = mc.fontRenderer.FONT_HEIGHT;
        RenderUtil.drawRectangleCorrectly(X.getValue() - 4, Y.getValue() - 4, textWidth + 16, textHeight + 12, ColorUtil.toRGBA(0, 255, 255, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 4, textHeight + 4, ColorUtil.toRGBA(30, 30, 30, 255));
        RenderUtil.drawRectangleCorrectly(X.getValue(), Y.getValue(), textWidth + 8, textHeight + 4, ColorUtil.toRGBA(30, 30, 30, 255));
        mc.fontRenderer.drawString(message, X.getValue() + 3, Y.getValue() + 3, ColorUtil.toRGBA(255, 255, 255, 255), false);
    }
}
