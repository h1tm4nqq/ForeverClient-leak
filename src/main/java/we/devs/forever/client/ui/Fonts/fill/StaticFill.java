package we.devs.forever.client.ui.Fonts.fill;

import we.devs.forever.client.ui.Fonts.shape.AbstractShape;

import java.awt.*;

public class StaticFill implements Fill {
    private final Color color;

    public StaticFill(Color color) {
        this.color = color;
    }

    @Override
    public Color colorAt(AbstractShape abstractShape, float x, float y) {
        return color;
    }
}
