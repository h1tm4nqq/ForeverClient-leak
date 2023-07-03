package we.devs.forever.client.ui.foreverClientGui.hud.component;


import net.minecraft.client.network.NetworkPlayerInfo;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class PingHud extends Hud {
    public PingHud() {
        super("Ping");
    }

    @Override
    public void onRenderHud() {
//        if(!fullNullCheck()) {
            NetworkPlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUniqueID());
            String str = "Ping " + info.getResponseTime();
            renderText(str);
//        }
    }
}
