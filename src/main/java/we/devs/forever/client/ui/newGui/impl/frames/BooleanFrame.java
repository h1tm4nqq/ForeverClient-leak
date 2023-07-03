package we.devs.forever.client.ui.newGui.impl.frames;

import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.newGui.NewGui;
import we.devs.forever.client.ui.newGui.api.Frame;

import java.awt.*;

public class BooleanFrame extends Frame<Boolean> {


    public BooleanFrame(Setting<Boolean> setting) {
        super(setting);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NewGui.fontButtons.drawStringWithShadow(setting.getName(),
                x + 3.5F,
                isHovering(mouseX,mouseY) ? 2F + y: 3F + y, setting.getValue()
                        ? ClickGui.mainColor.getColor().getRGB()
                        : Color.WHITE.getRGB());

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(isHovering(mouseX,mouseY) && mouseButton == 0) {
            setting.setValue(!setting.getValue());
        }
    }

    @Override
    public void update() {
        this.setVisible(setting.isVisible());
    }
}
