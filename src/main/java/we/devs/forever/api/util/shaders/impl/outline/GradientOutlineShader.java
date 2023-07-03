package we.devs.forever.api.util.shaders.impl.outline;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public final class GradientOutlineShader extends FramebufferShader {
    public static final GradientOutlineShader INSTANCE;

    static {
        INSTANCE = new GradientOutlineShader();
    }

    public float time;

    public GradientOutlineShader() {
        super("outlineGradient.frag");
        this.time = 0.0f;
    }

    public void setupUniforms() {
        this.setupUniform("texture");
        this.setupUniform("texelSize");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");
        this.setupUniform("alpha0");
        this.setupUniform("resolution");
        this.setupUniform("time");
        this.setupUniform("moreGradient");
        this.setupUniform("Creepy");
        this.setupUniform("alpha");
        this.setupUniform("NUM_OCTAVES");
    }

    public void updateUniforms(final Color color, final float radius, final float quality, final boolean gradientAlpha, final int alphaOutline, final float duplicate, final float moreGradient, final float creepy, final float alpha, final int numOctaves) {
        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform2f(this.getUniform("texelSize"), 1.0f / this.mc.displayWidth * (radius * quality), 1.0f / this.mc.displayHeight * (radius * quality));
        GL20.glUniform3f(this.getUniform("color"), color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f);
        GL20.glUniform1f(this.getUniform("divider"), 140.0f);
        GL20.glUniform1f(this.getUniform("radius"), radius);
        GL20.glUniform1f(this.getUniform("maxSample"), 10.0f);
        GL20.glUniform1f(this.getUniform("alpha0"), gradientAlpha ? -1.0f : (alphaOutline / 255.0f));
        GL20.glUniform2f(this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate);
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform1f(this.getUniform("moreGradient"), moreGradient);
        GL20.glUniform1f(this.getUniform("Creepy"), creepy);
        GL20.glUniform1f(this.getUniform("alpha"), alpha);
        GL20.glUniform1i(this.getUniform("NUM_OCTAVES"), numOctaves);
    }

    public void stopDraw(final Color color, final float radius, final float quality, final boolean gradientAlpha, final int alphaOutline, final float duplicate, final float moreGradient, final float creepy, final float alpha, final int numOctaves) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(color, radius, quality, gradientAlpha, alphaOutline, duplicate, moreGradient, creepy, alpha, numOctaves);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final Color color, final float radius, final float quality, final boolean gradientAlpha, final int alphaOutline, final float duplicate, final float moreGradient, final float creepy, final float alpha, final int numOctaves) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap();
            this.setupUniforms();
        }
        this.updateUniforms(color, radius, quality, gradientAlpha, alphaOutline, duplicate, moreGradient, creepy, alpha, numOctaves);
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
