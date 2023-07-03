package we.devs.forever.client.ui.customScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import we.devs.forever.api.util.Util;
import we.devs.forever.client.ui.Fonts.CustomFont;

import java.awt.*;

public class SplashProgress implements Util {
    private static float Progress;
    private static String Current;
    private static CustomFont unicodeFontRenderer;

    static {
        Current = "";
    }

    public static void Update() {
        drawSplash(Minecraft.getMinecraft().getTextureManager());
    }

    public static void setProgress(float n, String string) {
        Progress = n;
        Current = string;
        Update();

    }

    public static void drawSplash(TextureManager textureManager) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int n = scaledResolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledResolution.getScaledWidth() * n, scaledResolution.getScaledHeight() * n, true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 0.0, 1000.0, 3000.0);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        ResourceLocation splash = new ResourceLocation("textures/mainscreen.png");
        textureManager.bindTexture(splash);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Gui.drawScaledCustomSizeModalRect(0, 0, 0.0f, 0.0f, 1920, 1080, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 1920.0f, 1080.0f);
        drawProgress();
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledResolution.getScaledWidth() * n, scaledResolution.getScaledHeight() * n);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1f);
        Minecraft.getMinecraft().updateDisplay();
    }

    private static void drawProgress() {
        if (Minecraft.getMinecraft().gameSettings == null) {
            return;
        }
        if (unicodeFontRenderer == null) {
//            unicodeFontRenderer = new UnicodeFontRenderer(new Font("/assets/minecraft/Font/moonhouse.ttf", 0, 17), 1, 1);
            unicodeFontRenderer = new CustomFont(new Font("/assets/minecraft/Font/moonhouse.ttf", 0, 17), true, true);
        }

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        double d = Progress;
        double d2 = d / 7.0 * scaledResolution.getScaledWidth();
        Gui.drawRect(0, (scaledResolution.getScaledHeight() - 35), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), new Color(0, 0, 0, 50).getRGB());
        unicodeFontRenderer.drawString(Current, 20, scaledResolution.getScaledHeight() - 25, -1);
        GlStateManager.resetColor();
        resetTextureState();
        String string = Progress + "/" + 7.0;
        unicodeFontRenderer.drawString(string, scaledResolution.getScaledWidth() - 20 - unicodeFontRenderer.getStringWidth(string), scaledResolution.getScaledHeight() - 25, -505290241);
        GlStateManager.resetColor();
        resetTextureState();
        Gui.drawRect(0, (scaledResolution.getScaledHeight() - 2), (int) d2, scaledResolution.getScaledHeight(), new Color(3, 35, 244).getRGB());
        Gui.drawRect(0, (scaledResolution.getScaledHeight() - 2), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), new Color(0, 0, 0, 10).getRGB());
    }

    private static void resetTextureState() {
        GlStateManager.bindTexture(-1);
    }
}
