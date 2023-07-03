package we.devs.forever.api.util.shaders.impl.fill;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public class FlowShader extends FramebufferShader {
    public static final FlowShader INSTANCE;

    static {
        INSTANCE = new FlowShader();
    }

    public float time;

    public FlowShader() {
        super("flow.frag");
    }

    public void setupUniforms() {
        this.setupUniform("resolution");
        this.setupUniform("time");
        this.setupUniform("color");
        this.setupUniform("iterations");
        this.setupUniform("formuparam2");
        this.setupUniform("stepsize");
        this.setupUniform("volsteps");
        this.setupUniform("zoom");
        this.setupUniform("tile");
        this.setupUniform("distfading");
        this.setupUniform("saturation");
        this.setupUniform("fadeBol");
    }

    public void updateUniforms(final float duplicate, final float red, final float green, final float blue, final float alpha, final int iteractions, final float formuparam2, final float zoom, final float volumSteps, final float stepSize, final float title, final float distfading, final float saturation, final int fade) {
        GL20.glUniform2f(this.getUniform("resolution"), new ScaledResolution(this.mc).getScaledWidth() / duplicate, new ScaledResolution(this.mc).getScaledHeight() / duplicate);
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform4f(this.getUniform("color"), red, green, blue, alpha);
        GL20.glUniform1i(this.getUniform("iterations"), iteractions);
        GL20.glUniform1f(this.getUniform("formuparam2"), formuparam2);
        GL20.glUniform1i(this.getUniform("volsteps"), (int) volumSteps);
        GL20.glUniform1f(this.getUniform("stepsize"), stepSize);
        GL20.glUniform1f(this.getUniform("zoom"), zoom);
        GL20.glUniform1f(this.getUniform("tile"), title);
        GL20.glUniform1f(this.getUniform("distfading"), distfading);
        GL20.glUniform1f(this.getUniform("saturation"), saturation);
        GL20.glUniform1i(this.getUniform("fadeBol"), fade);
    }

    public void stopDraw(final Color color, final float radius, final float quality, final float duplicate, final int iteractions, final float formuparam2, final float zoom, final float volumSteps, final float stepSize, final float title, final float distfading, final float saturation, final int fade) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.radius = radius;
        this.quality = quality;
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(duplicate, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), iteractions, formuparam2, zoom, volumSteps, stepSize, title, distfading, saturation, fade);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final float duplicate, final float red, final float green, final float blue, final float alpha, final int iteractions, final float formuparam2, final float zoom, final float volumSteps, final float stepSize, final float title, final float distfading, final float saturation,  final int fade) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap();
            this.setupUniforms();
        }
        this.updateUniforms(duplicate, red, green, blue, alpha, iteractions, formuparam2, zoom, volumSteps, stepSize, title, distfading, saturation, fade);
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
