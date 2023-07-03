package we.devs.forever.client.ui.newGui.impl.frames;

import org.lwjgl.input.Keyboard;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.newGui.NewGui;
import we.devs.forever.client.ui.newGui.api.Frame;

import java.awt.*;

public class BindFrame extends Frame<Bind> {


    public BindFrame(Setting<Bind> setting) {
        super(setting);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NewGui.fontButtons.drawStringWithShadow(setting.getName(),
                x + 3.5F,
                isHovering(mouseX,mouseY) ? 2F + y: 3F + y, setting.getValue().getKey() != Keyboard.KEY_NONE
                        ? ClickGui.mainColor.getColor().getRGB()
                        : Color.WHITE.getRGB());

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public void update() {
        this.setVisible(setting.isVisible());
    }
}
