package we.devs.forever.api.util.shaders.impl.fill;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public class RainbowShader extends FramebufferShader {
    public static final RainbowShader INSTANCE;

    static {
        INSTANCE = new RainbowShader();
    }

    public float time;

    public RainbowShader() {
        super("Rainbow.frag");

    }

    public void setupUniforms() {
        this.setupUniform("color");
        this.setupUniform("time");
        this.setupUniform("resolution");
        this.setupUniform("texture");
    }

    public void updateUniforms(final float red, final float green, final float blue, final float alpha) {
        GL20.glUniform4f(this.getUniform("color"), red, green, blue, alpha);
        GL20.glUniform2f(this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth(), new ScaledResolution(this.mc).getScaledHeight());
        GL20.glUniform1f(this.getUniform("time"), this.time);
    }

    public void stopDraw(final Color color) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final float red, final float green, final float blue, final float alpha) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<>();
            this.setupUniforms();
        }
        this.updateUniforms(red, green, blue, alpha);
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
