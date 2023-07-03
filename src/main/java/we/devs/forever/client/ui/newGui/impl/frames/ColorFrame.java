package we.devs.forever.client.ui.newGui.impl.frames;

import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.newGui.NewGui;
import we.devs.forever.client.ui.newGui.api.Frame;

import java.awt.*;

public class ColorFrame extends Frame<Color> {


    public ColorFrame(Setting<Color> setting) {
        super(setting);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NewGui.fontButtons.drawStringWithShadow(setting.getName(),
                x + 3.5F,
                isHovering(mouseX,mouseY) ? 2F + y: 3F + y, Color.WHITE.getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void update() {
        this.setVisible(setting.isVisible());
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }
}
