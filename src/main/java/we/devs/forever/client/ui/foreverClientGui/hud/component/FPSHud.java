package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.Minecraft;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class FPSHud extends Hud {
    public FPSHud() {
        super("FPS");
    }

    @Override
    public void onRenderHud() {
        renderText("FPS " + fpsManagemer.getFPS());
    }
}