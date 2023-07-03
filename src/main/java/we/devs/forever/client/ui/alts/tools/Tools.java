package we.devs.forever.client.ui.alts.tools;

import net.minecraft.client.gui.Gui;

public class Tools {
    public static void drawBorderedRect(int x, int y, int x1, int y1, int size, int borderColor, int insideColor) {
        Gui.drawRect(x + size, y + size, x1 - size, y1 - size, insideColor);
        Gui.drawRect(x + size, y + size, x1, y, borderColor);
        Gui.drawRect(x, y, x + size, y1, borderColor);
        Gui.drawRect(x1, y1, x1 - size, y + size, borderColor);
        Gui.drawRect(x, y1 - size, x1, y1, borderColor);
    }
}

