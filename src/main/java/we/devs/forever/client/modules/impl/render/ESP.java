package we.devs.forever.client.modules.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.events.render.RenderEntityModelEvent;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.awt.*;

public
class ESP extends Module {

    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Outline));
    private final Setting<Boolean> players = (new Setting<>("Players", true));
    private final Setting<Boolean> animals = (new Setting<>("Animals", false));
    private final Setting<Boolean> mobs = (new Setting<>("Mobs", false));
    private final Setting<Boolean> items = (new Setting<>("Items", false));
    private final Setting<Integer> red = (new Setting<>("Red", 255, 0, 255));
    private final Setting<Integer> green = (new Setting<>("Green", 255, 0, 255));
    private final Setting<Integer> blue = (new Setting<>("Blue", 255, 0, 255));
    private final Setting<Integer> boxAlpha = (new Setting<>("BoxAlpha", 120, 0, 255));
    private final Setting<Integer> alpha = (new Setting<>("Alpha", 255, 0, 255));
    private final Setting<Float> lineWidth = (new Setting<>("LineWidth", 2.0f, 0.1f, 5.0f));
    private final Setting<Boolean> colorFriends = (new Setting<>("Friends", true));
    private final Setting<Boolean> self = (new Setting<>("Self", true));
    private final Setting<Boolean> onTop = (new Setting<>("onTop", true));
    private final Setting<Boolean> invisibles = (new Setting<>("Invisibles", false));

    public ESP() {
        super("ESP", "Renders a nice ESP.", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (items.getValue()) {
            int i = 0;
            for (Entity entity : mc.world.loadedEntityList) {
                if (((entity instanceof EntityItem)) && mc.player.getDistanceSq(entity) < 2500) {
                    Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                    AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);
                    GL11.glEnable(2848);
                    GL11.glHint(3154, 4354);
                    GL11.glLineWidth(1.0f);
                    RenderGlobal.renderFilledBox(bb, red.getValue() / 255.0f, green.getValue() / 255.0f, blue.getValue() / 255.0f, boxAlpha.getValue() / 255.0f);
                    GL11.glDisable(2848);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableDepth();
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                    RenderUtil.drawBlockOutline(bb, new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), 1.0f, true);
                    i++;
                    if (i >= 50) {
                        break;
                    }
                }
            }
        }
    }

    public void onRenderModel(RenderEntityModelEvent event) {
        if (event.getStage() != 0 || event.entity == null || (event.entity.isInvisible() && !invisibles.getValue()) || (!self.getValue() && event.entity.equals(mc.player)) || (!players.getValue() && event.entity instanceof EntityPlayer) || (!animals.getValue() && EntityUtil.isPassive(event.entity)) || (!mobs.getValue() && !EntityUtil.isPassive(event.entity) && !(event.entity instanceof EntityPlayer))) {
            return;
        }

        Color color = EntityUtil.getColor(event.entity, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue(), colorFriends.getValue());
        boolean fancyGraphics = mc.gameSettings.fancyGraphics;
        mc.gameSettings.fancyGraphics = false;
        float gamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 10000f;

        if (onTop.getValue()) {
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
        }

        if (mode.getValue() == Mode.Outline) {
            RenderUtil.renderOne(lineWidth.getValue());
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
            GlStateManager.glLineWidth(lineWidth.getValue());
            RenderUtil.renderTwo();
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
            GlStateManager.glLineWidth(lineWidth.getValue());
            RenderUtil.renderThree();
            RenderUtil.renderFour(color);
            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );
            GlStateManager.glLineWidth(lineWidth.getValue());
            RenderUtil.renderFive();
        } else {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            if (mode.getValue() == Mode.Wireframe) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            } else {
                GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);

            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            GlStateManager.glLineWidth(lineWidth.getValue());

            event.modelBase.render(
                    event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale
            );

            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

        if (!onTop.getValue()) {
            event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale);
        }

        try {
            mc.gameSettings.fancyGraphics = fancyGraphics;
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception ignored) {
        }

        event.cancel();
    }

    public
    enum Mode {
        Wireframe,
        Outline
    }
}
