package we.devs.forever.api.util.render.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.render.RenderUtil;

import java.awt.*;

public class RenderPlayer implements Util {
    public static void renderPlayer(EntityPlayer player,double[] poss, Color fill, Color outline, float lineWidth) {
        StaticModelPlayer model = new StaticModelPlayer(player, player instanceof AbstractClientPlayer && ((AbstractClientPlayer) player).getSkinType().equals("slim"), 0);
        model.disableArmorLayers();
        double x = poss[0] - mc.getRenderManager().viewerPosX;
        double y = poss[1] - mc.getRenderManager().viewerPosY;
        double z = poss[2] - mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        RenderUtil.startRender();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180 - model.getYaw(), 0, 1, 0);

        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        double widthX = player.getEntityBoundingBox().maxX - player.getRenderBoundingBox().minX + 1;
        double widthZ = player.getEntityBoundingBox().maxZ - player.getEntityBoundingBox().minZ + 1;

        GlStateManager.scale(widthX, player.height, widthZ);

        GlStateManager.translate(0.0F, -1.501F, 0.0F);

        RenderUtil.setColor(fill);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        model.render(0.0625f);

        RenderUtil.setColor(outline);
        GL11.glLineWidth(lineWidth);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        model.render(0.0625f);

        RenderUtil.endRender();
        GlStateManager.popMatrix();
    }
}
