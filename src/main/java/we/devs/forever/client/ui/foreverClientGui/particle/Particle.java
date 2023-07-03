package we.devs.forever.client.ui.foreverClientGui.particle;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;

import javax.vecmath.Vector2f;
import java.util.concurrent.ThreadLocalRandom;

public class Particle extends GuiScreen {
    private final int maxAlpha;
    private Vector2f pos;
    private Vector2f velocity;
    private Vector2f acceleration;
    private int alpha;
    private float size;
    private float guiSize; //не придумал умнее залупу для норм изменения размера

    public Particle(Vector2f pos) {
        this.pos = pos;
        int lowVel = -1;
        int highVel = 1;
        float resultXVel = (float) lowVel + ThreadLocalRandom.current().nextFloat() * (float) (highVel - lowVel);
        float resultYVel = (float) lowVel + ThreadLocalRandom.current().nextFloat() * (float) (highVel - lowVel);
        this.velocity = new Vector2f(resultXVel, resultYVel);
        this.acceleration = new Vector2f(0.0F, 0.35F);
        this.alpha = 0;
        this.maxAlpha = ThreadLocalRandom.current().nextInt(32, 192);
        this.size = ClickGui.getInstance().particleSize.getValue() + ThreadLocalRandom.current().nextFloat() * 1.5F;
        guiSize = ClickGui.getInstance().particleSize.getValue();
    }

    public static int changeAlpha(int origColor, int userInputedAlpha) {
        return userInputedAlpha << 24 | (origColor &= 16777215);
    }

    public void respawn(ScaledResolution scaledResolution) {
        this.pos = new Vector2f((float) (Math.random() * (double) scaledResolution.getScaledWidth()), (float) (Math.random() * (double) scaledResolution.getScaledHeight()));
    }

    public void update() {
        if (guiSize != ClickGui.getInstance().particleSize.getValue()) {
            this.size = ClickGui.getInstance().particleSize.getValue() + ThreadLocalRandom.current().nextFloat() * 1.5F;
            guiSize = ClickGui.getInstance().particleSize.getValue();
        }
        if (this.alpha < this.maxAlpha) {
            this.alpha += 8;
        }

        if (this.acceleration.getX() > 0.35F) {
            this.acceleration.setX(this.acceleration.getX() * 0.975F);
        } else if (this.acceleration.getX() < -0.35F) {
            this.acceleration.setX(this.acceleration.getX() * 0.975F);
        }

        if (this.acceleration.getY() > 0.35F) {
            this.acceleration.setY(this.acceleration.getY() * 0.975F);
        } else if (this.acceleration.getY() < -0.35F) {
            this.acceleration.setY(this.acceleration.getY() * 0.975F);
        }

        this.pos.add(this.acceleration);
        this.pos.add(this.velocity);
    }

    public void render(int mouseX, int mouseY) {
        if (Mouse.isButtonDown(0)) {
            float deltaXToMouse = (float) mouseX - this.pos.getX();
            float deltaYToMouse = (float) mouseY - this.pos.getY();
            if (Math.abs(deltaXToMouse) < 50.0F && Math.abs(deltaYToMouse) < 50.0F) {
                this.acceleration.setX(this.acceleration.getX() + deltaXToMouse * 0.0015F);
                this.acceleration.setY(this.acceleration.getY() + deltaYToMouse * 0.0015F);
            }
        }

        RenderUtil.drawRect(this.pos.x, this.pos.y, this.pos.x + this.size, this.pos.y + this.size, ClickGui.getInstance().particleColor.getColor().getRGB());
    }

    public Vector2f getPos() {
        return this.pos;
    }

    public void setPos(Vector2f pos) {
        this.pos = pos;
    }

    public Vector2f getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public Vector2f getAcceleration() {
        return this.acceleration;
    }

    public void setAcceleration(Vector2f acceleration) {
        this.acceleration = acceleration;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public float getSize() {
        return this.size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}
