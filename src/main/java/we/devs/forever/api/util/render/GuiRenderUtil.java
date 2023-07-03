package we.devs.forever.api.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import we.devs.forever.api.util.Util;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class GuiRenderUtil implements Util {
    private static final Frustum frustrum;
    private static final FloatBuffer screenCoords;
    private static final IntBuffer viewport;
    private static final FloatBuffer modelView;
    private static final FloatBuffer projection;
    public static RenderItem itemRender;
    public static ICamera camera;
    private static boolean depth;
    private static boolean texture;
    private static boolean clean;
    private static boolean bind;
    private static boolean override;

    static {
        itemRender = mc.getRenderItem();
        camera = new Frustum();
        frustrum = new Frustum();
        depth = GL11.glIsEnabled(2896);
        texture = GL11.glIsEnabled(3042);
        clean = GL11.glIsEnabled(3553);
        bind = GL11.glIsEnabled(2929);
        override = GL11.glIsEnabled(2848);
        screenCoords = BufferUtils.createFloatBuffer(3);
        viewport = BufferUtils.createIntBuffer(16);
        modelView = BufferUtils.createFloatBuffer(16);
        projection = BufferUtils.createFloatBuffer(16);
    }

    protected float zLevel;

    public static void drawRectangleCorrectly(int x, int y, int w, int h, int color) {
        GL11.glLineWidth(1.0F);
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    public static void drawGradientSideways(final double left, final double top, final double right, final double bottom, final int col1, final int col2) {
        final float f = (col1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (col1 & 0xFF) / 255.0f;
        final float f5 = (col2 >> 24 & 0xFF) / 255.0f;
        final float f6 = (col2 >> 16 & 0xFF) / 255.0f;
        final float f7 = (col2 >> 8 & 0xFF) / 255.0f;
        final float f8 = (col2 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);
        GL11.glColor4f(f6, f7, f8, f5);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    public static void drawIndicator(AxisAlignedBB bb, int argb, int sides) {
        final int a = (argb >>> 24) & 0xFF;
        final int r = (argb >>> 16) & 0xFF;
        final int g = (argb >>> 8) & 0xFF;
        final int b = argb & 0xFF;
        drawIndicator(bb, r, g, b, a, sides);
    }

    public static void drawIndicator(AxisAlignedBB bb, int r, int g, int b, int a, int sides) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        }

        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 0).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 0).endVertex();
        }

        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 0).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 0).endVertex();
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, 0).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, 0).endVertex();
        }

        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, 0).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, 0).endVertex();
        }
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    public static void drawGradientBlockOutline(AxisAlignedBB bb, Color startColor, Color endColor, float linewidth) {
        float red = (float) startColor.getRed() / 255.0F;
        float green = (float) startColor.getGreen() / 255.0F;
        float blue = (float) startColor.getBlue() / 255.0F;
        float alpha = (float) startColor.getAlpha() / 255.0F;
        float red1 = (float) endColor.getRed() / 255.0F;
        float green1 = (float) endColor.getGreen() / 255.0F;
        float blue1 = (float) endColor.getBlue() / 255.0F;
        float alpha1 = (float) endColor.getAlpha() / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(linewidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    /**
     * {@link GL11} scissor using minecraft screen positions.
     *
     * @param xPosition X start location
     * @param yPosition Y start location
     * @param width     scissor width
     * @param height    scissor height
     **/
    public static void glScissor(double xPosition, double yPosition, double width, double height) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glScissor((int) ((xPosition * Minecraft.getMinecraft().displayWidth) / scaledResolution.getScaledWidth()),
                (int) (((scaledResolution.getScaledHeight() - (yPosition + height)) * Minecraft.getMinecraft().displayHeight) / scaledResolution.getScaledHeight()),
                (int) (width * Minecraft.getMinecraft().displayWidth / scaledResolution.getScaledWidth()),
                (int) (height * Minecraft.getMinecraft().displayHeight / scaledResolution.getScaledHeight()));
    }

    /**
     * Draw line on screen.
     *
     * @param x     x start location
     * @param y     y start location
     * @param x1    x end location
     * @param y1    y end location
     * @param color color of line
     **/
    public static void drawLine(float x, float y, float x1, float y1, float width, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(width);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        BufferBuilder worldrenderer = Tessellator.getInstance().getBuffer();
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x, y, 0.0D).endVertex();
        worldrenderer.pos(x1, y1, 0.0D).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glLineWidth(2.0F);
        GlStateManager.bindTexture(0);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Draw filled circle on screen.
     *
     * @param xPosition x start location
     * @param yPosition y start location
     * @param radius    radius of circle
     * @param color     color of circle
     **/
    public static void drawCircle(float xPosition, float yPosition, float radius, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        BufferBuilder worldRenderer = Tessellator.getInstance().getBuffer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(xPosition, yPosition, 0).endVertex();
        for (int i = 0; i <= 100; i++) {
            double angle = (Math.PI * 2 * i / 100) + Math.toRadians(180);
            worldRenderer.pos(xPosition + Math.sin(angle) * radius, yPosition + Math.cos(angle) * radius, 0).endVertex();
        }
        Tessellator.getInstance().draw();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.bindTexture(0);
    }

    /**
     * Draw hollow circle on screen.
     *
     * @param xPosition x start location
     * @param yPosition y start location
     * @param radius    radius of circle
     * @param color     color of circle
     * @param thickness width of circle outline
     **/
    public static void drawHollowCircle(float xPosition, float yPosition, float radius, float thickness, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(3042);
        GL11.glLineWidth(thickness);
        GL11.glBegin(2);
        for (int i = 0; i < 70; i++) {
            float x = radius * MathHelper.cos((float) (i * 0.08975979010256552D));
            float y = radius * MathHelper.sin((float) (i * 0.08975979010256552D));
            GL11.glVertex2f(xPosition + x, yPosition + y);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GlStateManager.popMatrix();
        GL11.glLineWidth(2.0F);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.bindTexture(0);
    }

    /**
     * Same as {@link #drawCircle(float, float, float, Color)} accept can be confided
     * by start angle and end angle.
     *
     * @param xPosition  x start location
     * @param yPosition  y start location
     * @param radius     radius of angle
     * @param startAngle Start orientation of angle
     * @param endAngle   end orientation of angle
     * @param color      color of angle
     * @implNote Strait Angles - 90|180|270|360
     **/
    public static void drawPartialCircle(float xPosition, float yPosition, float radius, int startAngle, int endAngle, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        BufferBuilder worldRenderer = Tessellator.getInstance().getBuffer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(xPosition, yPosition, 0).endVertex();
        for (int i = (int) (startAngle / 360.0 * 100); i <= (int) (endAngle / 360.0 * 100); i++) {
            double angle = (Math.PI * 2 * i / 100) + Math.toRadians(180);
            worldRenderer.pos(xPosition + Math.sin(angle) * radius, yPosition + Math.cos(angle) * radius, 0).endVertex();
        }
        Tessellator.getInstance().draw();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.bindTexture(0);
    }

    /**
     * Same as {@link #drawHollowCircle(float, float, float, float, Color)} accept can be confided
     * by start angle and end angle.
     *
     * @param xPosition  x start location
     * @param yPosition  y start location
     * @param radius     radius of angle
     * @param startAngle Start orientation of angle
     * @param endAngle   end orientation of angle
     * @param color      color of angle
     * @param thickness  width of circle outline
     * @implNote Strait Angles - 90|180|270|360
     **/
    public static void drawHollowPartialCircle(float xPosition, float yPosition, float radius, int startAngle, int endAngle, float thickness, Color color) {
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glLineWidth(thickness);
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        GL11.glBegin(3);
        float f1 = 0.017453292F;
        for (int j = startAngle; j <= endAngle; ++j) {
            float f = (float) (j - 90) * f1;
            GL11.glVertex2f(xPosition + (float) Math.cos(f) * radius, yPosition + (float) Math.sin(f) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(2.0F);
        GL11.glPopMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.bindTexture(0);
    }

    /**
     * Draw rectangle on screen.
     *
     * @param xPosition x start location
     * @param yPosition y start location
     * @param width     width of rectangle
     * @param height    height of rectangle
     * @param color     color of rectangle
     **/
    public static void drawRectangle(float xPosition, float yPosition, float width, float height, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        BufferBuilder worldrenderer = Tessellator.getInstance().getBuffer();
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(xPosition, yPosition + height, 0.0D).endVertex();
        worldrenderer.pos(xPosition + width, yPosition + height, 0.0D).endVertex();
        worldrenderer.pos(xPosition + width, yPosition, 0.0D).endVertex();
        worldrenderer.pos(xPosition, yPosition, 0.0D).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Same as {@link #drawRectangle(float, float, float, float, Color)} accept corners can be adjusts
     * by angle measure.
     *
     * @param x x location
     * @param y y location
     * @param width     width of rectangle
     * @param height    height of rectangle
     * @param angle     angle of rectangle corners
     * @param color     color of rectangle
     **/
    public static void drawBorderedRoundedRectangle(float x, float y, float width, float height, float angle, float outlineWidth, Color color, Color outlineColor) {
        drawRoundedRectangle(x-outlineWidth , y -outlineWidth, width+ outlineWidth *2F,height+ outlineWidth *2F,angle,outlineColor);
        drawRoundedRectangle(x,y,width,height,angle,color);
    }
    public static void drawRoundedRectangle(float x, float y, float width, float height, float angle, Color color) {
        drawPartialCircle(x + angle, y + angle, angle, 0, 90, color);
        drawPartialCircle(x + width - angle, y + angle, angle, 270, 360, color);
        drawPartialCircle(x + width - angle, y + height - angle, angle, 180, 270, color);
        drawPartialCircle(x + angle, y + height - angle, angle, 90, 180, color);
        drawRectangle(x + angle, y, width - (angle * 2), angle, color);
        drawRectangle(x + angle, y + height - angle, width - (angle * 2), angle, color);
        drawRectangle(x, y + angle, width, height - (angle * 2), color);
    }

    /**
     * Same as {@link #drawRectangle(float, float, float, float, Color)} but with a border surrounding it
     *
     * @param xPosition   x start location
     * @param yPosition   y start location
     * @param width       width of rectangle
     * @param height      height of rectangle
     * @param borderWidth width of rectangle border
     * @param color       color of rectangle
     * @param borderColor color of rectangle border
     **/
    public static void drawBorderedRectangle(float xPosition, float yPosition, float width, float height, float borderWidth, Color color, Color borderColor) {
        drawRectangle(xPosition, yPosition, width, height, color);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glPushMatrix();
        GlStateManager.color((float) borderColor.getRed() / 255.0F, (float) borderColor.getGreen() / 255.0F, (float) borderColor.getBlue() / 255.0F, (float) borderColor.getAlpha() / 255.0F);
        GL11.glLineWidth(borderWidth);
        GL11.glBegin(1);
        GL11.glVertex2d(xPosition, yPosition);
        GL11.glVertex2d(xPosition, (yPosition + height));
        GL11.glVertex2d((xPosition + width), (yPosition + height));
        GL11.glVertex2d((xPosition + width), yPosition);
        GL11.glVertex2d(xPosition, yPosition);
        GL11.glVertex2d((xPosition + width), yPosition);
        GL11.glVertex2d(xPosition, (yPosition + height));
        GL11.glVertex2d((xPosition + width), (yPosition + height));
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    /**
     * Draw textured rectangle on screen.
     *
     * @param resourceLocation ResourceLocation of texture
     * @param xPosition        x start location
     * @param yPosition        y start location
     * @param width            width of rectangle
     * @param height           height of rectangle
     **/
    public static void drawTexturedRectangle(ResourceLocation resourceLocation, float xPosition, float yPosition, float width, float height) {
        GL11.glEnable(GL11.GL_BLEND);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(xPosition, yPosition);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(xPosition, yPosition + height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(xPosition + width, yPosition + height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(xPosition + width, yPosition);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.bindTexture(0);
    }

    /**
     * Draw polygon based on number of sides.
     *
     * @param xPosition x start location
     * @param yPosition y start location
     * @param radius    radius of polygon
     * @param sides     number of sides in polygon
     * @param color     color of polygon
     **/
    public static void drawRegularPolygon(float xPosition, float yPosition, float radius, float sides, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        BufferBuilder worldrenderer = Tessellator.getInstance().getBuffer();
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(xPosition, yPosition, 0).endVertex();
        for (int i = 0; i <= sides; i++) {
            double angle = ((Math.PI * 2) * i / sides) + Math.toRadians(180);
            worldrenderer.pos(xPosition + Math.sin(angle) * radius, yPosition + Math.cos(angle) * radius, 0).endVertex();
        }
        Tessellator.getInstance().draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.bindTexture(0);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    public static void drawGradientRect(float x, float y, float w, float h, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double) x + (double) w, y, 0.0D).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(x, y, 0.0D).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(x, (double) y + (double) h, 0.0D).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos((double) x + (double) w, (double) y + (double) h, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }




    public static void drawLine(float x, float y, float x1, float y1, float thickness, int hex) {
        float red = (float) (hex >> 16 & 255) / 255.0F;
        float green = (float) (hex >> 8 & 255) / 255.0F;
        float blue = (float) (hex & 255) / 255.0F;
        float alpha = (float) (hex >> 24 & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GL11.glLineWidth(thickness);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GL11.glDisable(2848);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }




    public static void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }




    public static void glEnd() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
        GL11.glEnable(2929);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }




    public static void drawArc(float cx, float cy, float r, float start_angle, float end_angle, int num_segments) {
        GL11.glBegin(4);

        for (int i = (int) ((float) num_segments / (360.0F / start_angle)) + 1; (float) i <= (float) num_segments / (360.0F / end_angle); ++i) {
            double previousangle = 6.283185307179586D * (double) (i - 1) / (double) num_segments;
            double angle = 6.283185307179586D * (double) i / (double) num_segments;
            GL11.glVertex2d(cx, cy);
            GL11.glVertex2d((double) cx + Math.cos(angle) * (double) r, (double) cy + Math.sin(angle) * (double) r);
            GL11.glVertex2d((double) cx + Math.cos(previousangle) * (double) r, (double) cy + Math.sin(previousangle) * (double) r);
        }

        glEnd();
    }

    public static void drawArcOutline(float cx, float cy, float r, float start_angle, float end_angle, int num_segments) {
        GL11.glBegin(2);

        for (int i = (int) ((float) num_segments / (360.0F / start_angle)) + 1; (float) i <= (float) num_segments / (360.0F / end_angle); ++i) {
            double angle = 6.283185307179586D * (double) i / (double) num_segments;
            GL11.glVertex2d((double) cx + Math.cos(angle) * (double) r, (double) cy + Math.sin(angle) * (double) r);
        }

        glEnd();
    }

    public static void drawCircleOutline(float x, float y, float radius) {
        drawCircleOutline(x, y, radius, 0, 360, 40);
    }

    public static void drawCircleOutline(float x, float y, float radius, int start, int end, int segments) {
        drawArcOutline(x, y, radius, (float) start, (float) end, segments);
    }

    public static void drawCircle(float x, float y, float radius) {
        drawCircle(x, y, radius, 0, 360, 64);
    }

    public static void drawCircle(float x, float y, float radius, int start, int end, int segments) {
        drawArc(x, y, radius, (float) start, (float) end, segments);
    }

    public static void drawOutlinedRoundedRectangle(int x, int y, int width, int height, float radius, float dR, float dG, float dB, float dA, float outlineWidth) {
        drawRoundedRectangle((float) x, (float) y, (float) width, (float) height, radius);
        GL11.glColor4f(dR, dG, dB, dA);
        drawRoundedRectangle((float) x + outlineWidth, (float) y + outlineWidth, (float) width - outlineWidth * 2.0F, (float) height - outlineWidth * 2.0F, radius);
    }

    public static void drawRectangle(float x, float y, float width, float height) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glBegin(2);
        GL11.glVertex2d(width, 0.0D);
        GL11.glVertex2d(0.0D, 0.0D);
        GL11.glVertex2d(0.0D, height);
        GL11.glVertex2d(width, height);
        glEnd();
    }

    public static void drawRectangleXY(float x, float y, float width, float height) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glBegin(2);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        glEnd();
    }

    public static void drawFilledRectangle(float x, float y, float width, float height) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glBegin(7);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        glEnd();
    }

    public static Vec3d to2D(double x, double y, double z) {
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);
        return result ? new Vec3d(screenCoords.get(0), (float) Display.getHeight() - screenCoords.get(1), screenCoords.get(2)) : null;
    }

    public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
        boolean blend = GL11.glIsEnabled(3042);
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        hexColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x - size / widthDiv, y + size);
        GL11.glVertex2d(x, y + size / heightDiv);
        GL11.glVertex2d(x + size / widthDiv, y + size);
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        if (outline) {
            GL11.glLineWidth(outlineWidth);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, alpha);
            GL11.glBegin(2);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x - size / widthDiv, y + size);
            GL11.glVertex2d(x, y + size / heightDiv);
            GL11.glVertex2d(x + size / widthDiv, y + size);
            GL11.glVertex2d(x, y);
            GL11.glEnd();
        }

        GL11.glPopMatrix();
        GL11.glEnable(3553);
        if (!blend) {
            GL11.glDisable(3042);
        }

        GL11.glDisable(2848);
    }

    public static int getRainbow(int speed, int offset, float s, float b) {
        float hue = (float) ((System.currentTimeMillis() + (long) offset) % (long) speed);
        hue /= (float) speed;
        return Color.getHSBColor(hue, s, b).getRGB();
    }

    public static void hexColor(int hexColor) {
        float red = (float) (hexColor >> 16 & 255) / 255.0F;
        float green = (float) (hexColor >> 8 & 255) / 255.0F;
        float blue = (float) (hexColor & 255) / 255.0F;
        float alpha = (float) (hexColor >> 24 & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void drawRoundedRectangle(float x, float y, float width, float height, float radius) {
        GL11.glEnable(3042);
        drawArc(x + width - radius, y + height - radius, radius, 0.0F, 90.0F, 16);
        drawArc(x + radius, y + height - radius, radius, 90.0F, 180.0F, 16);
        drawArc(x + radius, y + radius, radius, 180.0F, 270.0F, 16);
        drawArc(x + width - radius, y + radius, radius, 270.0F, 360.0F, 16);
        GL11.glBegin(4);
        GL11.glVertex2d(x + width - radius, y);
        GL11.glVertex2d(x + radius, y);
        GL11.glVertex2d(x + width - radius, y + radius);
        GL11.glVertex2d(x + width - radius, y + radius);
        GL11.glVertex2d(x + radius, y);
        GL11.glVertex2d(x + radius, y + radius);
        GL11.glVertex2d(x + width, y + radius);
        GL11.glVertex2d(x, y + radius);
        GL11.glVertex2d(x, y + height - radius);
        GL11.glVertex2d(x + width, y + radius);
        GL11.glVertex2d(x, y + height - radius);
        GL11.glVertex2d(x + width, y + height - radius);
        GL11.glVertex2d(x + width - radius, y + height - radius);
        GL11.glVertex2d(x + radius, y + height - radius);
        GL11.glVertex2d(x + width - radius, y + height);
        GL11.glVertex2d(x + width - radius, y + height);
        GL11.glVertex2d(x + radius, y + height - radius);
        GL11.glVertex2d(x + radius, y + height);
        glEnd();
    }

    public static void renderOne(float lineWidth) {
        checkSetupFBO();
        GL11.glPushAttrib(1048575);
        GL11.glDisable(3008);
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(2848);
        GL11.glEnable(2960);
        GL11.glClear(1024);
        GL11.glClearStencil(15);
        GL11.glStencilFunc(512, 1, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6913);
    }

    public static void renderTwo() {
        GL11.glStencilFunc(512, 0, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6914);
    }

    public static void renderThree() {
        GL11.glStencilFunc(514, 1, 15);
        GL11.glStencilOp(7680, 7680, 7680);
        GL11.glPolygonMode(1032, 6913);
    }

    public static void renderFour(Color color) {
        setColor(color);
        GL11.glDepthMask(false);
        GL11.glDisable(2929);
        GL11.glEnable(10754);
        GL11.glPolygonOffset(1.0F, -2000000.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderFive() {
        GL11.glPolygonOffset(1.0F, 2000000.0F);
        GL11.glDisable(10754);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(2960);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glEnable(3042);
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glEnable(3008);
        GL11.glPopAttrib();
    }

    public static void setColor(Color color) {
        GL11.glColor4d((double) color.getRed() / 255.0D, (double) color.getGreen() / 255.0D, (double) color.getBlue() / 255.0D, (double) color.getAlpha() / 255.0D);
    }

    public static void checkSetupFBO() {
        Framebuffer fbo = mc.framebuffer;
        if (fbo != null && fbo.depthBuffer > -1) {
            setupFBO(fbo);
            fbo.depthBuffer = -1;
        }

    }

    private static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferID);
    }

    public static void drawBorderedRect(double left, double top, double right, double bottom, double width, int internalColor, int borderColor) {
        enableGL2D();
        fakeGuiRect(left + width, top + width, right - width, bottom - width, internalColor);
        fakeGuiRect(left + width, top, right - width, top + width, borderColor);
        fakeGuiRect(left, top, left + width, bottom, borderColor);
        fakeGuiRect(right - width, top, right, bottom, borderColor);
        fakeGuiRect(left + width, bottom - width, right - width, bottom, borderColor);
        disableGL2D();
    }

    private static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void fakeGuiRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 0xFF) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public void drawUGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, this.zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, this.zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, this.zLevel).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, this.zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static class RenderTesselator extends Tessellator {
        public static RenderUtil.RenderTesselator INSTANCE = new RenderUtil.RenderTesselator();

        public RenderTesselator() {
            super(2097152);
        }

        public static void prepare(int mode) {
            prepareGL();
            begin(mode);
        }

        public static void prepareGL() {
            GL11.glBlendFunc(770, 771);
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.glLineWidth(1.5F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }

        public static void begin(int mode) {
            INSTANCE.getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
        }

        public static void release() {
            render();
            releaseGL();
        }

        public static void render() {
            INSTANCE.draw();
        }

        public static void releaseGL() {
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
        }

        public static void drawBox(BlockPos blockPos, int argb, int sides) {
            int a = argb >>> 24 & 255;
            int r = argb >>> 16 & 255;
            int g = argb >>> 8 & 255;
            int b = argb & 255;
            drawBox(blockPos, r, g, b, a, sides);
        }

        public static void drawBox(float x, float y, float z, int argb, int sides) {
            int a = argb >>> 24 & 255;
            int r = argb >>> 16 & 255;
            int g = argb >>> 8 & 255;
            int b = argb & 255;
            drawBox(INSTANCE.getBuffer(), x, y, z, 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
        }

        public static void drawBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
            drawBox(INSTANCE.getBuffer(), (float) blockPos.getX(), (float) blockPos.getY(), (float) blockPos.getZ(), 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
        }

        public static BufferBuilder getBufferBuilder() {
            return INSTANCE.getBuffer();
        }

        public static void drawBox(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
            if ((sides & 1) != 0) {
                buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x, y, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 2) != 0) {
                buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 4) != 0) {
                buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 8) != 0) {
                buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 16) != 0) {
                buffer.pos(x, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 32) != 0) {
                buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            }

        }

        public static void drawLines(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
            if ((sides & 17) != 0) {
                buffer.pos(x, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 18) != 0) {
                buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 33) != 0) {
                buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 34) != 0) {
                buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 5) != 0) {
                buffer.pos(x, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 6) != 0) {
                buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 9) != 0) {
                buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 10) != 0) {
                buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 20) != 0) {
                buffer.pos(x, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 36) != 0) {
                buffer.pos(x + w, y, z).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z).color(r, g, b, a).endVertex();
            }

            if ((sides & 24) != 0) {
                buffer.pos(x, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x, y + h, z + d).color(r, g, b, a).endVertex();
            }

            if ((sides & 40) != 0) {
                buffer.pos(x + w, y, z + d).color(r, g, b, a).endVertex();
                buffer.pos(x + w, y + h, z + d).color(r, g, b, a).endVertex();
            }

        }

        public static void drawBoundingBox(AxisAlignedBB bb, float width, float red, float green, float blue, float alpha) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(width);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
            bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
            tessellator.draw();
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        public static void drawFullBox(AxisAlignedBB bb, BlockPos blockPos, float width, int argb, int alpha2) {
            int a = argb >>> 24 & 255;
            int r = argb >>> 16 & 255;
            int g = argb >>> 8 & 255;
            int b = argb & 255;
            drawFullBox(bb, blockPos, width, r, g, b, a, alpha2);
        }

        public static void drawFullBox(AxisAlignedBB bb, BlockPos blockPos, float width, int red, int green, int blue, int alpha, int alpha2) {
            prepare(7);
            drawBox(blockPos, red, green, blue, alpha, 63);
            release();
            drawBoundingBox(bb, width, (float) red, (float) green, (float) blue, (float) alpha2);
        }

        public static void drawHalfBox(BlockPos blockPos, int argb, int sides) {
            int a = argb >>> 24 & 255;
            int r = argb >>> 16 & 255;
            int g = argb >>> 8 & 255;
            int b = argb & 255;
            drawHalfBox(blockPos, r, g, b, a, sides);
        }

        public static void drawHalfBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
            drawBox(INSTANCE.getBuffer(), (float) blockPos.getX(), (float) blockPos.getY(), (float) blockPos.getZ(), 1.0F, 0.5F, 1.0F, r, g, b, a, sides);
        }
    }

    public static final class GeometryMasks {
        public static final HashMap FACEMAP = new HashMap();

        static {
            FACEMAP.put(EnumFacing.DOWN, 1);
            FACEMAP.put(EnumFacing.WEST, 16);
            FACEMAP.put(EnumFacing.NORTH, 4);
            FACEMAP.put(EnumFacing.SOUTH, 8);
            FACEMAP.put(EnumFacing.EAST, 32);
            FACEMAP.put(EnumFacing.UP, 2);
        }

        public static final class Line {
            public static final int DOWN_WEST = 17;
            public static final int UP_WEST = 18;
            public static final int DOWN_EAST = 33;
            public static final int UP_EAST = 34;
            public static final int DOWN_NORTH = 5;
            public static final int UP_NORTH = 6;
            public static final int DOWN_SOUTH = 9;
            public static final int UP_SOUTH = 10;
            public static final int NORTH_WEST = 20;
            public static final int NORTH_EAST = 36;
            public static final int SOUTH_WEST = 24;
            public static final int SOUTH_EAST = 40;
            public static final int ALL = 63;
        }

        public static final class Quad {
            public static final int DOWN = 1;
            public static final int UP = 2;
            public static final int NORTH = 4;
            public static final int SOUTH = 8;
            public static final int WEST = 16;
            public static final int EAST = 32;
            public static final int ALL = 63;
        }
    }
}
