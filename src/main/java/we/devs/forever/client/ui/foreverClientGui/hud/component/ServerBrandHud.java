package we.devs.forever.client.ui.foreverClientGui.hud.component;


import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class ServerBrandHud extends Hud {
    public ServerBrandHud() {
        super("ServerBrand");
    }

    @Override
    public void onRenderHud() {
        renderText("Server brand " + serverManager.getServerBrand());
    }
}
