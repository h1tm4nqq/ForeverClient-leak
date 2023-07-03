package we.devs.forever.client.ui.foreverClientGui.hud.component;

import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class CoordsHud extends Hud {
    public Setting<Boolean> direction = (new Setting<>("Direction", true));

    public CoordsHud() {
        super("Coords");
    }

    @Override
    public void onRenderHud() {
        boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

        int posX = (int) mc.player.posX;
        int posY = (int) mc.player.posY;
        int posZ = (int) mc.player.posZ;

        float nether = !inHell ? 0.125f : 8;
        int hposX = (int) (mc.player.posX * nether);
        int hposZ = (int) (mc.player.posZ * nether);

        renderText(direction.getValue() ? rotationManager.getDirection4D(false) + " " + posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]" :  posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]");
    }
}