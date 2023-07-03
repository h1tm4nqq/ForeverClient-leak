package we.devs.forever.client.modules.impl.render;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class ChinaHat extends Module {

    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Circle));
    public Setting<Boolean> unRenderFirstPerson = (new Setting<>("UnRenderFirstPerson", true, "If you turn on this module you will see the hat from first person"));
    public Setting<Float> height = (new Setting<>("Height", 0.3f, 0.1f, 1.0f, "Height of the hat"));
    public Setting<Float> yPos = (new Setting<>("YPos", 0.3f, 0.1f, 2.0f, "How high will the hat be above your head"));
    public Setting<Color> color = (new Setting<>("Color", new Color(255, 255, 255, 99), ColorPickerButton.Mode.Normal, 100, "The hat color"));
    public ChinaHat() {
        super("ChinaHat", "China hat", Category.RENDER);
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        ItemStack stack = mc.player.getActiveItemStack();
        final double height = stack.getItem() instanceof ItemArmor ? mc.player.isSneaking() ? -0.18 : 0.04 : mc.player.isSneaking() ? -0.30 : -0.08;
        if ((mc.gameSettings.thirdPersonView == 1 || mc.gameSettings.thirdPersonView == 2) && unRenderFirstPerson.getValue()) {
            final float red = color.getColor().getRed() / 255.0f;
            final float green = color.getColor().getGreen() / 255.0f;
            final float blue = color.getColor().getBlue() / 255.0f;
            final float alpha = color.getColor().getAlpha() / 255.0f;
            GL11.glPushMatrix();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glVertex3d(0.0, this.height.getValue(), 0.0);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            GL11.glColor4f(red, green, blue, alpha);
            GL11.glTranslatef(0f, (float) ((mc.player.height + this.yPos.getValue()) + height), 0f);
            GL11.glRotatef(90f, 1f, 0f, 0f);
            Cylinder c = new Cylinder();
            c.setDrawStyle(GLU.GLU_SMOOTH);
            c.setDrawStyle(GLU.GLU_LINE);

            if (this.mode.getValue() == Mode.StrawCircle) {
                c.draw(0f, 0.55f, this.height.getValue(), 30, 5);
            }

            if (this.mode.getValue() == Mode.StrawHexagon) {
                c.draw(0f, 0.55f, this.height.getValue(), 5, 10);
            }

            if (this.mode.getValue() == Mode.Circle) {
                c.draw(0f, 0.55f, this.height.getValue(), 150, 100);
            }

            if (this.mode.getValue() == Mode.Hexagon) {
                c.draw(0f, 0.55f, this.height.getValue(), 6, 100);
            }

            if (this.mode.getValue() == Mode.StrawPolygon) {
                c.draw(0f, 0.55f, this.height.getValue(), 9, 10);
            }

            if (this.mode.getValue() == Mode.Polygon) {
                c.draw(0f, 0.55f, this.height.getValue(), 9, 100);
            }

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        } else if (!unRenderFirstPerson.getValue()) {
            GL11.glPushMatrix();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glVertex3d(0.0, this.height.getValue(), 0.0);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            final float red = color.getColor().getRed() / 255.0f;
            final float green = color.getColor().getGreen() / 255.0f;
            final float blue = color.getColor().getBlue() / 255.0f;
            final float alpha = color.getColor().getAlpha() / 255.0f;
            GL11.glColor4f(red, green, blue, alpha);
            GL11.glTranslatef(0f, (float) ((mc.player.height + this.yPos.getValue()) + height), 0f);
            GL11.glRotatef(90f, 1f, 0f, 0f);
            Cylinder c = new Cylinder();
            c.setDrawStyle(GLU.GLU_SMOOTH);
            c.setDrawStyle(GLU.GLU_LINE);
            if (this.mode.getValue() == Mode.StrawCircle) {
                c.draw(0f, 0.55f, this.height.getValue(), 30, 5);
            }

            if (this.mode.getValue() == Mode.StrawHexagon) {
                c.draw(0f, 0.55f, this.height.getValue(), 5, 10);
            }

            if (this.mode.getValue() == Mode.Circle) {
                c.draw(0f, 0.55f, this.height.getValue(), 150, 100);
            }

            if (this.mode.getValue() == Mode.Hexagon) {
                c.draw(0f, 0.55f, this.height.getValue(), 6, 100);
            }

            if (this.mode.getValue() == Mode.StrawPolygon) {
                c.draw(0f, 0.55f, this.height.getValue(), 9, 10);
            }

            if (this.mode.getValue() == Mode.Polygon) {
                c.draw(0f, 0.55f, this.height.getValue(), 9, 100);
            }

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    public enum Mode {
        StrawCircle,
        StrawHexagon,
        StrawPolygon,
        Circle,
        Hexagon,
        Polygon
    }
}