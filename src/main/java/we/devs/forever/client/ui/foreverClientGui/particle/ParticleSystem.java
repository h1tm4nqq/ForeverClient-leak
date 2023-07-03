package we.devs.forever.client.ui.foreverClientGui.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

public final class ParticleSystem extends GuiScreen {
    private final int PARTS = 200;
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final ScaledResolution scaledResolution;


    public ParticleSystem() {
        scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        for (int i = 0; i < PARTS; ++i) {
            particles.add(new Particle(
                    new Vector2f((float) (Math.random() * scaledResolution.getScaledWidth()),
                            (float) (Math.random() * scaledResolution.getScaledHeight()))));
        }
    }

    public static double map(double value, double a, double b, double c, double d) {
        value = (value - a) / (b - a);
        return c + value * (d - c);
    }

    public void update() {
        particles.parallelStream().forEach(particle -> {
            boolean isOffScreenX = particle.getPos().x > (float) this.scaledResolution.getScaledWidth() || particle.getPos().x < 0.0F;
            boolean isOffScreenY = particle.getPos().y > (float) this.scaledResolution.getScaledHeight() || particle.getPos().y < 0.0F;
            if (isOffScreenX || isOffScreenY) {
                particle.respawn(this.scaledResolution);
            }

            particle.update();
        });
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < PARTS; ++i) {
            Particle particle = this.particles.get(i);
            for (int j = 1; j < PARTS; ++j) {
                if (i != j) {
                    Particle otherParticle = this.particles.get(j);
                    Vector2f diffPos = new Vector2f(particle.getPos());
                    diffPos.sub(otherParticle.getPos());
                    float diff = diffPos.length();
                    int distance = ClickGui.getInstance().particleLength.getValue() / (this.scaledResolution.getScaleFactor() <= 1 ? 3 : this.scaledResolution.getScaleFactor());
                    if (diff < (float) distance) {
                        int lineAlpha = (int) map(diff, distance, 0.0, 0.0, 127.0);
                        if (lineAlpha > 8) {
                            RenderUtil.drawLine(particle.getPos().x + particle.getSize() / 2.0F, particle.getPos().y + particle.getSize() / 2.0F, otherParticle.getPos().x + otherParticle.getSize() / 2.0F, otherParticle.getPos().y + otherParticle.getSize() / 2.0F, 1.0F, ClickGui.getInstance().lineColor.getColor().getRGB());
                        }
                    }
                }
            }

            particle.render(mouseX, mouseY);
        }
    }

}