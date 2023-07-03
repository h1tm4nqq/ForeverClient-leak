package we.devs.forever.client.ui.Fonts.fill;



import we.devs.forever.client.ui.Fonts.shape.AbstractShape;

import java.awt.*;

public interface Fill {
    Color colorAt(AbstractShape abstractShape, float x, float y);
}
