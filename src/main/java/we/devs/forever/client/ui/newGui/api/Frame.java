package we.devs.forever.client.ui.newGui.api;

import we.devs.forever.client.Client;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.Component;

public abstract class Frame<T> extends Client {
    public static float scale;

    protected float x, y, height, width;
    protected boolean isVisible;
    protected Setting<T> setting;

    public Frame(Setting<T> setting) {
        this.setting = setting;
        height = 13 * scale;
        width = 100 * scale;
        isVisible = false;
    }

    public abstract void drawScreen(int mouseX, int mouseY, float partialTicks);


    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);

    public abstract void update();


    public void updateVisibility() {

    }
    public void updateScreen() {

    }
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public void keyTyped(char typedChar, int keyCode) {

    }


    protected boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    protected boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return x < mouseX && width > mouseX && y < mouseY && height > mouseY;
    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

}
