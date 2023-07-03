package we.devs.forever.client.ui.foreverClientGui.components;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.items.Item;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.Button;

import java.awt.*;
import java.util.ArrayList;

import static we.devs.forever.client.modules.impl.client.ClickGui.getInstance;

public class Component extends Client {
    private final ArrayList<Item> items = new ArrayList();
    public boolean drag;
    ResourceLocation image;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private float width;
    private float height;
    private boolean open;
    private boolean hidden = false;
    private int w;

    public Component(String name, ResourceLocation image, int x, int y, boolean open) {
        super(name);
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = 100* ForeverClientGui.getScale();
        this.height = 20* ForeverClientGui.getScale();
        this.open = open;
        this.setupItems();
    }

    public void setupItems() {
    }

    private void drag(int mouseX, int mouseY) {
        if (!drag) {
            return;
        }
        x = x2 + mouseX;
        y = y2 + mouseY;
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        drag(mouseX, mouseY);
        float totalItemHeight = open ? getTotalItemHeight() - 2.0F : 0.0f;

       RenderUtil.drawRect(x, y - 1.5f, x + width, y + height - 6, getInstance().mainColor.getColor());

            // GuiRenderUtil.drawGradientSideways(x, (float) y - 1.5f, x + width, y + height - 6, new Color(0, 0, 255, 255).getRGB(), color);

        //BAR
        if (open) {
            GuiRenderUtil.drawGradientSideways(x, (float) y + 12.5f, x + width, y + height + totalItemHeight, getInstance().getBgColor().getRGB(), getInstance().getBgColor().getRGB());
            GuiRenderUtil.drawBorderedRect(x + 0.4, (float) y + 12.6f - 13, x + width + 0.1, (y + height) + totalItemHeight + 0.1, ClickGui.getInstance().getOutlineWidth() - ClickGui.getInstance().getOutlineWidth() * 2, new Color(0, 0, 0, 0).getRGB(), getInstance().getModuleColor().getRGB());
        }
        if (!open) {
            GuiRenderUtil.drawBorderedRect(x, (float) y - 1.5f, x + width, y + height - 6, ClickGui.getInstance().getOutlineWidth() - ClickGui.getInstance().getOutlineWidth() * 2, new Color(0, 0, 0, 0).getRGB(), getInstance().getModuleColor().getRGB());
        }
        textManager.drawStringWithShadow(getName(), (float) x + 1.9F, (float) y - 6.2F + textManager.getFontHeight(), -1);
        // RenderUtil.drawRect(x + Config.INSTANCE.textureX.getValue(), y + Config.INSTANCE.textureY.getValue(),  Config.INSTANCE.textureSize.getValue(), Config.INSTANCE.textureSize.getValue(), new Color(10, 10, 10, 50).getRGB());
      if(ClickGui.images.getValue()) {
          mc.getTextureManager().bindTexture(image);
          RenderUtil.drawCompleteImage(x + 82.6F* ForeverClientGui.getScale(), y - 1.9F* ForeverClientGui.getScale(), 15F * ForeverClientGui.getScale(), 15F* ForeverClientGui.getScale());

      }
        if (open) {
            float y = (getY() + getHeight()) - 3.0f ;
            for (Item item : getItems()) {
                if (item.isHidden()) continue;
                item.setLocation((float) x + 2.0f, y);
                item.setWidth(getWidth() - 4);
                item.drawScreen(mouseX, mouseY, partialTicks);
                y += item.getHeight() + 1.5f;
            }
        }
        GL11.glPopMatrix();
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            x2 = x - mouseX;
            y2 = y - mouseY;
            ForeverClientGui.getClickGui().getComponents().forEach(component -> {
                if (!component.drag) return;
                component.drag = false;
            });
            drag = true;
            return;
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            open = !open;
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            return;
        }
        if (!open) {
            return;
        }
        getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) {
            drag = false;
        }
        if (!open) {
            return;
        }
        getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }



    public void onKeyTyped(char typedChar, int keyCode) {
        if (!open) {
            return;
        }
        getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void addButton(Button button) {
        items.add(button);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isOpen() {
        return open;
    }

    public final ArrayList<Item> getItems() {
        return items;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        if (mouseX < getX()) return false;
        if (mouseX > getX() + getWidth()) return false;
        if (mouseY < getY()) return false;
        return !(mouseY > getY() + getHeight() - (open ? 2 : 0));
    }

    private float getTotalItemHeight() {
        float height = 0.0f;
        for (Item item : getItems()) {
            height += item.getHeight() + 1.5f;
        }
        return height;
    }
}

