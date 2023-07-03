package we.devs.forever.client.ui.notification;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.hud.HUD;

public
class Notifications {
    private final String text;
    private final long disableTime;
    private final float width;
    private final TimerUtil timerUtil = new TimerUtil();

    public Notifications(String text, long disableTime) {
        this.text = text;
        this.disableTime = disableTime;
        this.width = Client.moduleManager.getModuleByClass(HUD.class).renderer.getStringWidth(text);
        timerUtil.reset();
    }

    public void onDraw(int y) {
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        if (timerUtil.passedMs(disableTime)) Client.notificationManager.getNotifications().remove(this);
        // GuiRenderUtil.drawBorderedRect(Config.INSTANCE.X.getValue(), Config.INSTANCE.Y.getValue(), Config.INSTANCE.X2.getValue(), Config.INSTANCE.Y2.getValue(), Config.INSTANCE.with.getValue(), new Color(12, 12, 12, 92).getRGB(), new Color(13, 218, 13, 255).getRGB());
        Client.moduleManager.getModuleByClass(HUD.class).renderer.drawString(text, scaledResolution.getScaledWidth() - width - 3, y + 2, -1, true);
    }
}
