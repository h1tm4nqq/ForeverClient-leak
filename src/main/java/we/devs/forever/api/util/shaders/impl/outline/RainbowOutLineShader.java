package we.devs.forever.api.util.shaders.impl.outline;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public class RainbowOutLineShader extends FramebufferShader {
    public static final RainbowOutLineShader INSTANCE;

    static {
        INSTANCE = new RainbowOutLineShader();
    }

    public float time;

    public RainbowOutLineShader() {
        super("OutLineRainbow.frag");

    }

    public void setupUniforms() {
        this.setupUniform("color");
        this.setupUniform("time");
        this.setupUniform("resolution");
        this.setupUniform("radius");
        this.setupUniform("texelSize");
    }

    public void updateUniforms(final float red, final float green, final float blue, final float alpha, final float radius, final float quality, final boolean gradientAlpha) {
        GL20.glUniform4f(this.getUniform("color"), red, green, blue, gradientAlpha ? -1.0f : alpha);
        GL20.glUniform2f(this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth(), new ScaledResolution(this.mc).getScaledHeight());
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(this.getUniform("texelSize"), 1.0f / this.mc.displayWidth * (radius * quality), 1.0f / this.mc.displayHeight * (radius * quality));
        GL20.glUniform1f(this.getUniform("radius"), radius);
    }

    public void stopDraw(final Color color, final float radius, final float quality, final boolean gradientAlpha) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f,radius,quality,gradientAlpha);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final float red, final float green, final float blue, final float alpha, final float radius, final float quality, final boolean gradientAlpha) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<>();
            this.setupUniforms();
        }
        this.updateUniforms(red, green, blue, alpha,radius,quality,gradientAlpha);
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
