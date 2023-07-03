package we.devs.forever.api.util.shaders.impl.fill;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public class GradientShader extends FramebufferShader {
    public static final GradientShader INSTANCE;

    static {
        INSTANCE = new GradientShader();
    }

    public float time;

    public GradientShader() {
        super("gradient.frag");
    }

    public void setupUniforms() {
        this.setupUniform("resolution");
        this.setupUniform("time");
        this.setupUniform("moreGradient");
        this.setupUniform("Creepy");
        this.setupUniform("alpha");
        this.setupUniform("NUM_OCTAVES");
    }

    public void updateUniforms(final float duplicate, final float moreGradient, final float creepy, final float alpha, final int numOctaves) {
        GL20.glUniform2f(this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate);
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform1f(this.getUniform("moreGradient"), moreGradient);
        GL20.glUniform1f(this.getUniform("Creepy"), creepy);
        GL20.glUniform1f(this.getUniform("alpha"), alpha);
        GL20.glUniform1i(this.getUniform("NUM_OCTAVES"), numOctaves);
    }

    public void stopDraw(final Color color, final float radius, final float quality, final float duplicate, final float moreGradient, final float creepy, final float alpha, final int numOctaves) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.red = color.getRed() / 255.0f;
        this.green = color.getGreen() / 255.0f;
        this.blue = color.getBlue() / 255.0f;
        this.radius = radius;
        this.quality = quality;
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(duplicate, moreGradient, creepy, alpha, numOctaves);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final float duplicate, final float moreGradient, final float creepy, final float alpha, final int numOctaves) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap();
            this.setupUniforms();
        }
        this.updateUniforms(duplicate, moreGradient, creepy, alpha, numOctaves);
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
