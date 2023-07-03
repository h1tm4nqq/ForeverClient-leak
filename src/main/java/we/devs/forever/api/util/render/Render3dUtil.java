package we.devs.forever.api.util.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import we.devs.forever.api.util.Util;

import java.awt.*;

public class Render3dUtil implements Util {

    public static void drawImageInBlock(AxisAlignedBB pos, Color color, Color outLineColor, ResourceLocation image) {
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();


   //     RenderUtil.glColor(color);
        RenderUtil.glBillboardDistanceScaled((float) pos.minX + 0.5f, (float) pos.minY + 0.5f, (float) pos.minZ + 0.5f, RenderUtil.mc.player, 1.0f);
//        GlStateManager.enableAlpha();
        RenderUtil.drawCircle(1.5f, -5, 16.0f,color);

//        RenderUtil.drawCircle(1.5f, -5, 16.5f);
//        RenderUtil.glColor(outLineColor);
//        GlStateManager.translate(0F,0F,0F);
         GlStateManager.enableTexture2D();
        mc.getTextureManager().bindTexture(image);
        Gui.drawScaledCustomSizeModalRect(-10, -17, 0, 0, 12, 12, 24, 24, 12, 12);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
