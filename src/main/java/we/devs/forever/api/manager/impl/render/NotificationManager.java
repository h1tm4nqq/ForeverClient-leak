package we.devs.forever.api.manager.impl.render;


import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.hud.HUD;
import we.devs.forever.client.ui.notification.Notifications;

import java.util.ArrayList;

public
class NotificationManager extends AbstractManager {
    private final ArrayList<Notifications> notifications = new ArrayList<>();

    public NotificationManager() {
        super("NotificationManager");
    }

    public void handleNotifications(int posY) {
        for (int i = 0; i < getNotifications().size(); i++) {
            getNotifications().get(i).onDraw(posY);
            posY -= Client.moduleManager.getModuleByClass(HUD.class).renderer.getFontHeight() + 5;
        }
    }

    public void addNotification(String text, long duration) {
        getNotifications().add(new Notifications(text, duration));
    }

    public ArrayList<Notifications> getNotifications() {
        return notifications;
    }

    @Override
    public void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}
