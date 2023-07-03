package we.devs.forever.client.ui.newGui.impl;

import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.modules.impl.client.Config1;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.Component;
import we.devs.forever.client.ui.newGui.NewGui;
import we.devs.forever.client.ui.newGui.api.Frame;
import we.devs.forever.client.ui.newGui.impl.frames.ModuleFrame;

import java.awt.*;
import java.util.ArrayList;

public class Panel extends Client {
    private float height, width, x, y, diffX, diffY;
    private final float scaleFrame = 13F * Frame.scale;
    private boolean open = true, drag = false;
    private final ArrayList<ModuleFrame> moduleFrames;
    private final Module.Category category;
    private TimeAnimation openAnim, closeAnim;

    public Panel(Module.Category category, ArrayList<ModuleFrame> moduleFrames, float x, float y) {
        this.x = x;
        this.y = y;
        this.moduleFrames = moduleFrames;
        height = moduleFrames.size() * (scaleFrame + .5F* Frame.scale) + Config1.X1.getValue();
        this.category = category;
        openAnim = new TimeAnimation(200, 0, 1);
        closeAnim = new TimeAnimation(200, -1, 0);
        width = 100;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (drag) {
            x = mouseX + diffX;
            y = mouseY + diffY;
        }

        float current = (float) (moduleFrames.stream().mapToDouble(frame -> frame.getHeight() + .5* Frame.scale)).sum() * (open ? openAnim.getCurrentFloat() : -closeAnim.getCurrentFloat());
        if (current != 0)
            GuiRenderUtil.drawBorderedRectangle(x, y, width, current, ClickGui.getInstance().getOutlineWidth(), ClickGui.bgGuiColor.getColor(), ClickGui.mainColor.getColor());

        GuiRenderUtil.drawBorderedRectangle(x, y - 14.5F, width, 14.5F, ClickGui.getInstance().getOutlineWidth(), ClickGui.mainColor.getColor(), ClickGui.mainColor.getColor());
        NewGui.fontComponent.drawStringWithShadow(category.getName(), x + 22F, y - 15 + 4.5F, Color.WHITE.getRGB());
        float y = this.y + 1.5F * Frame.scale;
        if (current != 0) {
            for (ModuleFrame frame : moduleFrames) {
                if (this.y + current <= y) {
                    break;
                }
                frame.setX(x);
                frame.setY(y);
               // GuiRenderUtil.drawLine(x,y,x + width,y,1, Color.WHITE.getRGB());
                y += (frame.getHeight() +.5F* Frame.scale);
                frame.drawScreen(mouseX, mouseY, partialTicks);
            }
        }
        moduleFrames.forEach(frame ->{
            if(frame.isOpen())frame.anim.update();
            else frame.animRev.update();
        });
        if (open) {
            openAnim.update();
        } else {
            closeAnim.update();
        }
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (open) moduleFrames.forEach(frame -> frame.mouseClicked(mouseX, mouseY, mouseButton));
        if (isHovering(mouseX, mouseY, x, y - 14.5F, width, 14.5F)) {
            if (mouseButton == 0) {
                NewGui.panels.forEach(panel -> drag = false);
                diffX = x - mouseX;
                diffY = y - mouseY;
                drag = true;
            }
            if (mouseButton == 1) {
//                openAnim = new TimeAnimation(200, 0, height);
//                closeAnim = new TimeAnimation(200, -height, 0);
                moduleFrames.forEach(frame ->{
                    frame.anim = new TimeAnimation(200, 0, 1);
                    frame.animRev =new TimeAnimation(200, -1, 0);
                });
                openAnim = new TimeAnimation(200, 0, 1);
                closeAnim = new TimeAnimation(200, -1, 0);
                open = !open;
            }
        }

    }


    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) drag = false;
        if (open) moduleFrames.forEach(frame -> frame.mouseReleased(mouseX, mouseY, state));
    }


    public void updateScreen() {
        if (open) moduleFrames.forEach(ModuleFrame::updateScreen);

    }


    public void keyTyped(char typedChar, int keyCode) {
        if (open) moduleFrames.forEach(frame -> frame.keyTyped(typedChar, keyCode));

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

    public void setOpen(boolean open) {
        this.open = open;
    }


    protected boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return x < mouseX && width + x > mouseX && y < mouseY && height + y > mouseY;
    }
}
