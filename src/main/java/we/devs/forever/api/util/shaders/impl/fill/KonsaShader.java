package we.devs.forever.api.util.shaders.impl.fill;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public class KonsaShader extends FramebufferShader {
    public static final KonsaShader INSTANCE;

    static {
        INSTANCE = new KonsaShader();
    }

    public float time;

    public KonsaShader() {
        super("konsa.frag");
    }

    public void setupUniforms() {
        this.setupUniform("Fill");
        this.setupUniform("Color");
        this.setupUniform("SampleRadius");
        this.setupUniform("RenderOutline");
        this.setupUniform("OutlineFade");
        this.setupUniform("texelSize");

    }

    public void updateUniforms(float[] fillCol,float[] outCol,final float radius) {
        GL20.glUniform4f(this.getUniform("Fill"), fillCol[0], fillCol[1], fillCol[2], fillCol[3]);
        GL20.glUniform4f(this.getUniform("Color"), outCol[0], outCol[1], outCol[2], outCol[3]);
        GL20.glUniform1f(this.getUniform("SampleRadius"),  radius);
        GL20.glUniform1i(this.getUniform("RenderOutline"), 1);
        GL20.glUniform1i(this.getUniform("OutlineFade"), 0);
        GL20.glUniform2f(this.getUniform("texelSize"), 1.0f / this.mc.displayWidth * (radius), 1.0f / this.mc.displayHeight * (radius));
    }

    public void stopDraw(final Color color,final Color outColor, final float radius) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f,
                outColor.getRed() / 255.0f, outColor.getGreen() / 255.0f, outColor.getBlue() / 255.0f, outColor.getAlpha() / 255.0f,
                radius
                );
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final float red, final float green, final float blue, final float alpha,
                            final float outRed, final float outGreen, final float outBlue, final float outAlpha,
                             final float radius
                            ) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<>();
            this.setupUniforms();
        }
        this.updateUniforms(new float[]{red, green, blue, alpha},
                new float[]{outRed, outGreen, outBlue, outAlpha},
                radius
        );
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
