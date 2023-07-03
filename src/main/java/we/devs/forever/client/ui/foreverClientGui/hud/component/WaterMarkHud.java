package we.devs.forever.client.ui.foreverClientGui.hud.component;


import we.devs.forever.client.ui.foreverClientGui.hud.Hud;
import we.devs.forever.main.ForeverClient;

public class WaterMarkHud extends Hud {
    public WaterMarkHud() {
        super("WaterMark");
    }

    @Override
    public void onRenderHud() {
        renderText(ForeverClient.MODNAME + " v" +ForeverClient.MODVER);
    }
}
