package we.devs.forever.client.ui.foreverClientGui.hud.component;

import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class IPHud extends Hud {
    public IPHud() {
        super("IP");
    }

    @Override
    public void onRenderHud() {
        String str = (mc.isSingleplayer() ? "singleplayer" : "IP:" + mc.currentServerData.serverIP);
        renderText(str);
    }
}