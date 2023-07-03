package we.devs.forever.client.ui.foreverClientGui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.render.Render2DEvent;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.HudEditor;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.Fonts.CustomFont;

import java.awt.*;

public abstract class Hud extends Module {
    private final float offset = 1;
    protected Setting<Float> X = (new Setting<>("X", 100f, 0f, 1920f));
    protected Setting<Float> Y = (new Setting<>("Y", 100f, 0f, 1080f));
    protected Setting<Boolean> rainbow;
    protected Setting<Integer> rainbowSpeed;
    protected float width, height = 0;
    protected boolean bg = true;
    protected float coordX, coordY;
    protected float partialTicks = 1F;
    private boolean dragging, touching = false;
    private float diffX, diffY = 0;
    private float x1, y1, x2, y2;
    protected Anchor anchor;


    protected Hud(String name) {
        super(name, "", Category.HUD, false, true, -1, true, KeyMode.Release, true);
        rainbow = (new Setting<>("Rainbow", false));
        rainbowSpeed = (new Setting<>("Speed", 70, 0, 400, v -> rainbow.getValue()));

    }

    protected Hud(String name, boolean b) {
        super(name, "", Category.HUD, false, true, Keyboard.KEY_NONE, true, KeyMode.Release, true);
        rainbow = new Setting("Rainbow", false);
        rainbowSpeed = new Setting("Speed", 70, 0, 400, v -> rainbow.getValue());
    }

    protected abstract void onRenderHud();


    @Override
    public void onRender2D(Render2DEvent event) {
        setAnchor();
        partialTicks = event.partialTicks;
        X.setMax((float) mc.displayWidth);
        Y.setMax((float) mc.displayHeight);

        if (HudEditor.INSTANCE.isEnabled() && bg) {
            x1 = X.getValue() - offset;
            y1 = Y.getValue() - offset;
            x2 = X.getValue() + offset + width;
            y2 = Y.getValue() + offset + height;
            RenderUtil.drawRect(x1, y1, x2, y2, ColorUtil.toRGBA(30, 30, 30, dragging ? 150 : (touching ? 120 : 80)));
            RenderUtil.drawLine(x1, y1, x1, y2, 2, ColorUtil.toRGBA(50, 50, 50, 100));
            RenderUtil.drawLine(x2, y1, x2, y2, 2, ColorUtil.toRGBA(50, 50, 50, 100));
            RenderUtil.drawLine(x1, y1, x2, y1, 2, ColorUtil.toRGBA(50, 50, 50, 100));
            RenderUtil.drawLine(x1, y2, x2, y2, 2, ColorUtil.toRGBA(50, 50, 50, 100));
        }

        onRenderHud();
    }

    protected final void renderText(String text) {
        width = (float) textManager.getStringWidth(text);
        height = (float) textManager.getHeight();
        float width = 0.0f;
        int delay = 1;
        for (char c : text.toCharArray()) {
            String temp = String.valueOf(c);
            textManager.drawStringWithShadow(temp, X.getValue() + width - 2, Y.getValue(), rainbow.getValue() ? ColorUtil.rainbow(delay * rainbowSpeed.getValue()).getRGB() : new Color(255, 255, 255, 255).getRGB());
            width += (float) this.textManager.getStringWidth(temp);
            delay++;
        }
    }

    public final void mouseUpdated(int mouseX, int mouseY) {
        if (dragging) {
            //   if (x1 <= x.getMax() && x2 <= x.getMax() ) {
            coordX = mouseX + diffX;
            X.setValue(coordX);
            //    if ( y1 <= y.getMax() && y2 <= y.getMax()) {
            coordY = mouseY + diffY;
            Y.setValue(coordY);
            //   }


        }
        touching = isMouseHovering(mouseX, mouseY);
    }

    public final void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        if (isMouseHovering(mouseX, mouseY)) {
            dragging = true;
            diffX = X.getValue() - mouseX;
            diffY = Y.getValue() - mouseY;
        }
    }

    protected final boolean isMouseHovering(int mouseX, int mouseY) {
        return isMouseHovering(mouseX, mouseY, X.getValue() - offset, Y.getValue() - offset,
                X.getValue() + width + offset, Y.getValue() + height + offset);
    }

    public final void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        dragging = false;
    }

    protected final boolean isMouseHovering(float mouseX, float mouseY, float x1, float y1, float x2, float y2) {
        return x1 < mouseX && x2 > mouseX && y1 < mouseY && y2 > mouseY;
    }

    private  void setAnchor() {
        float x = X.getValue() + width / 2;
        float y = Y.getValue() + height / 2;
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (y >= sr.getScaledHeight() / 2F && x >= sr.getScaledWidth() / 2F) {
            anchor = Anchor.BOTTOM_RIGHT;
        } else if (y >= sr.getScaledHeight() / 2F && x <= sr.getScaledWidth() / 2F) {
            anchor = Anchor.BOTTOM_LEFT;
        } else if (y <= sr.getScaledHeight() / 2F && x >= sr.getScaledWidth() / 2F) {
            anchor = Anchor.TOP_RIGHT;
        } else if (y <= sr.getScaledHeight() / 2F && x <= sr.getScaledWidth() / 2F) {
            anchor = Anchor.TOP_LEFT;
        }
    }

    public enum Anchor {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }
}
