package we.devs.forever.client.ui.foreverClientGui.hud.component;


import we.devs.forever.client.ui.foreverClientGui.hud.Hud;


public class TestHud extends Hud {
    public TestHud() {
        super("Test");
    }

    @Override
    public void onRenderHud() {
        renderText("1:" + mc.player.rotationPitch + " , " + mc.player.rotationPitch
                + "2:" + mc.player.rotationYaw + " , " + mc.player.rotationPitch
        );
    }
}
