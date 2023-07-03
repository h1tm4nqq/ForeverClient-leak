package we.devs.forever.client.ui.foreverClientGui.components.items;

import we.devs.forever.client.Client;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

public class Item extends Client {

    protected float x, y;
    protected float width, height;
    private boolean hidden;

    public Item(String name) {
        super(name);
    }

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {

    }

    public void update() {
    }

    public void updateVisibility() {
    }

    public void onKeyTyped(char typedChar, int keyCode) {
    }

    public boolean isButtonOpen() {
        return false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height * ForeverClientGui.getScale();
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean setHidden(boolean hidden) {
        return this.hidden = hidden;
    }
}
