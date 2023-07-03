package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class CompassHud extends Hud {
    public CompassHud() {
        super("Compass");
        width = 37;
        height = 37;
    }
    public Setting<Compass> compass = (new Setting<>("Compass", Compass.Circle));
    public Setting<Integer> scale = (new Setting<>("Scale", 3, 0, 10));
    private static final double HALF_PI = Math.PI / 2;

    @Override
    public void onRenderHud() {
        final ScaledResolution sr = new ScaledResolution(mc);
        if (compass.getValue() == Compass.Line) {
            float playerYaw = mc.player.rotationYaw;
            float rotationYaw = MathUtil.wrap(playerYaw);
            RenderUtil.drawRect(X.getValue(), Y.getValue(), X.getValue() + 100, Y.getValue() + renderer.getHeight(), 0x75101010);
            RenderUtil.glScissor(X.getValue(), Y.getValue(), X.getValue() + 100, Y.getValue() + renderer.getHeight(), sr);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            final float zeroZeroYaw = MathUtil.wrap((float) (Math.atan2(0 - mc.player.posZ, 0 - mc.player.posX) * 180.0d / Math.PI) - 90.0f);
            RenderUtil.drawLine(X.getValue() - rotationYaw + (100 / 2) + zeroZeroYaw, Y.getValue() + 2, X.getValue() - rotationYaw + (100 / 2) + zeroZeroYaw, Y.getValue() + renderer.getHeight() - 2, 2, 0xFFFF1010);
            RenderUtil.drawLine((X.getValue() - rotationYaw + (100 / 2)) + 45, Y.getValue() + 2, (X.getValue() - rotationYaw + (100 / 2)) + 45, Y.getValue() + renderer.getHeight() - 2, 2, 0xFFFFFFFF);
            RenderUtil.drawLine((X.getValue() - rotationYaw + (100 / 2)) - 45, Y.getValue() + 2, (X.getValue() - rotationYaw + (100 / 2)) - 45, Y.getValue() + renderer.getHeight() - 2, 2, 0xFFFFFFFF);
            RenderUtil.drawLine((X.getValue() - rotationYaw + (100 / 2)) + 135, Y.getValue() + 2, (X.getValue() - rotationYaw + (100 / 2)) + 135, Y.getValue() + renderer.getHeight() - 2, 2, 0xFFFFFFFF);
            RenderUtil.drawLine((X.getValue() - rotationYaw + (100 / 2)) - 135, Y.getValue() + 2, (X.getValue() - rotationYaw + (100 / 2)) - 135, Y.getValue() + renderer.getHeight() - 2, 2, 0xFFFFFFFF);
            renderer.drawStringWithShadow("n", (X.getValue() - rotationYaw + (100 / 2)) + 180 - renderer.getStringWidth("n") / 2.0f, Y.getValue(), 0xFFFFFFFF);
            renderer.drawStringWithShadow("n", (X.getValue() - rotationYaw + (100 / 2)) - 180 - renderer.getStringWidth("n") / 2.0f, Y.getValue(), 0xFFFFFFFF);
            renderer.drawStringWithShadow("e", (X.getValue() - rotationYaw + (100 / 2)) - 90 - renderer.getStringWidth("e") / 2.0f, Y.getValue(), 0xFFFFFFFF);
            renderer.drawStringWithShadow("s", (X.getValue() - rotationYaw + (100 / 2)) - renderer.getStringWidth("s") / 2.0f, Y.getValue(), 0xFFFFFFFF);
            renderer.drawStringWithShadow("w", (X.getValue() - rotationYaw + (100 / 2)) + 90 - renderer.getStringWidth("w") / 2.0f, Y.getValue(), 0xFFFFFFFF);
            RenderUtil.drawLine((X.getValue() + 100 / 2), Y.getValue() + 1, (X.getValue() + 100 / 2), Y.getValue() + renderer.getHeight() - 1, 2, 0xFF909090);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            final double centerX = X.getValue();
            final double centerY = Y.getValue();
            for (Direction dir : Direction.values()) {
                double rad = getPosOnCompass(dir);
                renderer.drawStringWithShadow(dir.name(), (float) (centerX + getX(rad)), (float) (centerY + getY(rad)), dir == Direction.N ? 0xFFFF0000 : 0xFFFFFFFF);
            }
        }
    }

    private static double getPosOnCompass(Direction dir) {
        double yaw = Math.toRadians(MathHelper.wrapDegrees(mc.player.rotationYaw));
        int index = dir.ordinal();
        return yaw + (index * HALF_PI);
    }

    private double getX(double rad) {
        return Math.sin(rad) * (scale.getValue() * 10);
    }

    private double getY(double rad) {
        final double epicPitch = MathHelper.clamp(mc.player.rotationPitch + 30f, -90f, 90f);
        final double pitchRadians = Math.toRadians(epicPitch); // player pitch
        return Math.cos(rad) * Math.sin(pitchRadians) * (scale.getValue() * 10);
    }

    private enum Direction {
        N,
        W,
        S,
        E
    }

    public enum Compass {
        Circle,
        Line
    }

}