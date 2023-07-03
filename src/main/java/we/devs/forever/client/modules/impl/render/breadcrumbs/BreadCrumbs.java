package we.devs.forever.client.modules.impl.render.breadcrumbs;

import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BreadCrumbs
        extends Module {
    public static BreadCrumbs breadCrumbs;
    public static ArrayList<double[]> vecs;
    public Setting<RenderMode> renderModeSetting = (new Setting<>("Render Mode", RenderMode.Default));
    public Setting<Float> width = (new Setting<>("Width", 5f, 0.5f, 15f,
            v -> renderModeSetting.getValue() != RenderMode.Circle));
    public Setting<Boolean> timeout = (new Setting<>("Timeout", true));
    public Setting<Integer> length = (new Setting<>("Length", 15, 5, 40,
            v -> this.timeout.getValue() || renderModeSetting.getValue() == RenderMode.Default));
    public Setting<Color> colorSetting = (new Setting<>("Color", new Color(255, 255, 255, 255), ColorPickerButton.Mode.Normal, 100));
    public List<Vec3d> path;
    Color color;

    public BreadCrumbs() {
        super("BreadCrumbs", "Draws a small line behind you", Category.RENDER);
        breadCrumbs = this;
        vecs = new ArrayList<>();
        path = new ArrayList<>();
    }

    public static double M(double n) {
        if (n == Double.longBitsToDouble(Double.doubleToLongBits(1.7931000183463725E308) ^ 0x7FEFEB11C3AAD037L)) {
            return n;
        }
        if (n < Double.longBitsToDouble(Double.doubleToLongBits(1.1859585260803721E308) ^ 0x7FE51C5AEE8AD07FL)) {
            return n * Double.longBitsToDouble(Double.doubleToLongBits(-12.527781766526259) ^ 0x7FD90E3969654F8FL);
        }
        return n;
    }

    @Override
    public void onTick() {
        if (renderModeSetting.getValue() == RenderMode.Default) updateDefault();
        else if (renderModeSetting.getValue() == RenderMode.Circle) updateCircle();
    }

    @Override
    public void onDisable() {
        vecs.removeAll(vecs);
    }

    @Override
    public void onAltRender3D(float partialTicks) {
        if (Objects.requireNonNull(renderModeSetting.getValue()) == RenderMode.Circle) {
            RenderUtil.renderBreadCrumbs(path, colorSetting.getColor());
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (renderModeSetting.getValue() == RenderMode.Default)
            renderDefault();
    }

    private void updateDefault() {
        if (!path.isEmpty())
            path.clear();

        try {
            double renderPosX = mc.getRenderManager().renderPosX;
            double renderPosY = mc.getRenderManager().renderPosY;
            double renderPosZ = mc.getRenderManager().renderPosZ;

            double n = renderPosY + Double.longBitsToDouble(Double.doubleToLongBits(0.48965838138858014) ^ 0x7FDF56901B91AE07L);
            if (mc.player.isElytraFlying()) {
                n -= Double.longBitsToDouble(Double.doubleToLongBits(29.56900080933637) ^ 0x7FC591AA097B7F4BL);
            }
            vecs.add(new double[]{renderPosX, n - (double) mc.player.height, renderPosZ});

        } catch (Exception exception) {
            // empty catch block
        }

        if (vecs.size() > length.getValue()) {
            vecs.remove(0);
        }
    }

    private void updateCircle() {
        if (mc.player.lastTickPosX != mc.player.posX || mc.player.lastTickPosY != mc.player.posY || mc.player.lastTickPosZ != mc.player.posZ) {
            path.add(new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ));
        }

        if (timeout.getValue())
            while (path.size() > length.getValue()) {
                path.remove(0);
            }
    }

    private void renderDefault() {
        try {
            double renderPosX = mc.getRenderManager().renderPosX;
            double renderPosY = mc.getRenderManager().renderPosY;
            double renderPosZ = mc.getRenderManager().renderPosZ;
            this.color = colorSetting.getColor();
            float n = (float) this.color.getRed() / Float.intBitsToFloat(Float.floatToIntBits(0.49987957f) ^ 0x7D80F037);
            float n2 = (float) this.color.getGreen() / Float.intBitsToFloat(Float.floatToIntBits(0.4340212f) ^ 0x7DA13807);
            float n3 = (float) this.color.getBlue() / Float.intBitsToFloat(Float.floatToIntBits(0.0131841665f) ^ 0x7F270267);
            RenderUtil.prepareGL();
            GL11.glPushMatrix();
            GL11.glEnable(2848);
            GL11.glLineWidth(width.getValue());
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(width.getValue());
            GL11.glDepthMask(false);
            GL11.glBegin(3);
            for (double[] vec : vecs) {
                double d = 0;
                double m = M(Math.hypot(vec[0] - mc.player.posX, vec[1] - mc.player.posY));
                if (d > (double) length.getValue()) {
                    continue;
                }
                GL11.glColor4f(n, n2, n3, Float.intBitsToFloat(Float.floatToIntBits(14.099797f) ^ 0x7EE198C5) - (float) (m / (double) length.getValue()));
                GL11.glVertex3d(vec[0] - renderPosX, vec[1] - renderPosY, vec[2] - renderPosZ);
            }
            GL11.glEnd();
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
            RenderUtil.releaseGL();
        } catch (Exception exception) {
            // empty catch block
        }
    }

}