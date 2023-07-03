package we.devs.forever.client.ui.Fonts.shape;

import org.lwjgl.util.vector.Vector2f;

public class Rectangle extends AbstractShape {
    protected float width;
    protected float height;

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        createPoints();
        checkPoints();
    }

    @Override
    protected void createPoints() {
        points = new Vector2f[4];

        points[0] = new Vector2f(x, y);

        points[1] = new Vector2f(x, y + height);

        points[2] = new Vector2f(x + width, y + height);

        points[3] = new Vector2f(x + width, y);

    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
