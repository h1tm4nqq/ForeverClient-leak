package we.devs.forever.client.ui.foreverClientGui.hud.component;

import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class TPSHud extends Hud {
    public TPSHud() {
        super("TPS");
    }

    @Override
    public void onRenderHud() {
        renderText("TPS " + serverManager.getTPS());
    }
}