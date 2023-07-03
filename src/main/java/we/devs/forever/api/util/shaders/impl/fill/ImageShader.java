package we.devs.forever.api.util.shaders.impl.fill;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.api.util.shaders.FramebufferShader;

import java.awt.*;
import java.util.HashMap;

public class ImageShader extends FramebufferShader {
    public static final ImageShader INSTANCE;

    static {
        INSTANCE = new ImageShader();
    }

    public float time;

    public ImageShader() {
        super("image.frag");
    }


    public void setupUniforms() {
        setupUniform("texture");
        setupUniform("texelSize");
        setupUniform("color");
        setupUniform("divider");
        setupUniform("radius");
        setupUniform("maxSample");
        setupUniform("dimensions");
        setupUniform("blur");
        setupUniform("mixFactor");
        setupUniform("image");
        setupUniform("imageMix");
        setupUniform("useImage");
        setupUniform("alpha");
    }



    public void updateUniforms(final float red, final float green, final float blue, final float alpha, boolean blur, float mix,boolean useImage, float imageMix, Image image) {
        GL20.glUniform1i(getUniform("texture"), 0);
        GL20.glUniform2f(getUniform("texelSize"), 1F / mc.displayWidth * (radius * quality), 1F / mc.displayHeight * (radius * quality));
        GL20.glUniform3f(getUniform("color"), red, green, blue);
        GL20.glUniform1f(getUniform("divider"), 140F);
        GL20.glUniform1f(getUniform("radius"), radius);
        GL20.glUniform1f(getUniform("maxSample"), 10F);
        GL20.glUniform2f(getUniform("dimensions"), mc.displayWidth, mc.displayHeight);
        GL20.glUniform1i(getUniform("blur"), blur ? 1 : 0);
        GL20.glUniform1f(getUniform("mixFactor"), alpha);
        GL20.glUniform1f(getUniform("alpha"), mix);
        GL13.glActiveTexture(GL13.GL_TEXTURE8);
        if (useImage) {
            ResourceLocation texture = image.getImage();
            if (texture != null) {
                mc.getTextureManager().bindTexture(texture);
            }
        }

        GL20.glUniform1i(getUniform("image"), 8);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL20.glUniform1f(getUniform("imageMix"), imageMix);
        GL20.glUniform1i(getUniform("useImage"), useImage ? 1 : 0);

    }

    public void stopDraw(final Color color, boolean blur, float mix,boolean useImage, float imageMix, Image image) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f, blur,mix /255F,useImage,imageMix/255F,image);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void startShader(final float red, final float green, final float blue, final float alpha, boolean blur, float mix,boolean useImage, float imageMix, Image image) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<>();
            this.setupUniforms();
        }
        this.updateUniforms(red, green, blue, alpha, blur,mix,useImage,imageMix,image);
    }

    public void update(final double speed) {
        this.time += (float) speed;
    }
}
