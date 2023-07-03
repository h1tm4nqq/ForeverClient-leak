package we.devs.forever.client.ui.foreverClientGui.hud.component;

import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class SpeedHud extends Hud {
    public SpeedHud() {
        super("SpeedHud");
    }

    @Override
    public void onRenderHud() {
        renderText("Speed " + speedManager.getSpeedKpH() + " km/h");
    }
}
