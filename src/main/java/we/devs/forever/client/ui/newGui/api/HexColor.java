package we.devs.forever.client.ui.newGui.api;

import java.awt.*;

@SuppressWarnings("unused")
public enum HexColor {

    red(Color.RED.getRGB()),
    blue(Color.BLUE.getRGB()),
    yellow(Color.YELLOW.getRGB()),
    green (Color.GREEN.getRGB()),
    orange(Color.ORANGE.getRGB()),
    white (Color.WHITE.getRGB());
    private final int color;
    HexColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
