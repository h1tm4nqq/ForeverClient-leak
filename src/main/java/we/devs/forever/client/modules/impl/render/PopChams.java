package we.devs.forever.client.modules.impl.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.events.render.TotemPopEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.entity.StaticModelPlayer;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PopChams extends Module {

    private final Setting<Color> color =
            (new Setting<Color>("FillColor", new Color(0, 255, 255, 128), ColorPickerButton.Mode.Normal, 100));
    private final Setting<Color> outline =
            (new Setting<Color>("OutlineColor", new Color(0, 255, 255, 255), ColorPickerButton.Mode.Normal, 100));
    private final Setting<Float> lineWidth =
            (new Setting<>("LineWidth", 1f, 0f, 10f));
    public final Setting<Boolean> copyAnimations =
            (new Setting<>("CopyAnimations", true));
    public final Setting<Double> yAnimations =
            (new Setting<>("YAnimation", 0., -7., 7.));
    public final Setting<Integer> fadeTime =
            (new Setting<>("FadeTime", 1500, 0, 5000));

    public final Setting<Boolean> selfPop =
            (new Setting<>("SelfPop", false));

    public final Setting<Color> selfColor =
            (new Setting<>("SelfColor", new Color(80, 80, 255, 80), ColorPickerButton.Mode.Normal, 100, v -> selfPop.getValue()));

    public final Setting<Color> selfOutline =
            (new Setting<>("SelfOutline", new Color(80, 80, 255, 255), ColorPickerButton.Mode.Normal, 100, v -> selfPop.getValue()));
    protected final Setting<Boolean> friendPop =
            (new Setting<>("FriendPop", false));

    public final Setting<Color> friendColor =
            (new Setting<>("FriendColor", new Color(45, 255, 45, 80), ColorPickerButton.Mode.Normal, 100, v -> friendPop.getValue()));
    public final Setting<Color> friendOutline =
            (new Setting<>("FriendOutline", new Color(45, 255, 45, 255), ColorPickerButton.Mode.Normal, 100, v -> friendPop.getValue()));
    public final List<PopData> popDataList = new CopyOnWriteArrayList<>();
    public static PopChams popChams;

    public PopChams() {
        super("PopChams", "Render players totem pops", Category.RENDER);
        popChams = this;
    }

    protected Color getColor(EntityPlayer entity) {
        if (entity.equals(mc.player)) {
            return this.selfColor.getColor();
        } else if (friendManager.isFriend(entity)) {
            return this.friendColor.getColor();
        } else {
            return this.color.getColor();
        }
    }

    protected Color getOutlineColor(EntityPlayer entity) {
        if (entity.equals(mc.player)) {
            return this.selfOutline.getColor();
        } else if (friendManager.isFriend(entity)) {
            return this.friendOutline.getColor();
        } else {
            return this.outline.getColor();
        }
    }

    protected boolean isValidEntity(EntityPlayer entity) {
        return !(entity == mc.player && !this.selfPop.getValue()) && !((friendManager.isFriend(entity) && entity != mc.player) && !this.friendPop.getValue());
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        popDataList
//                .filter(Objects::nonNull)
//                .collect(Collectors.toCollection(LinkedList::new))
                .forEach(data -> {
            EntityPlayer player = data.getPlayer();
            StaticModelPlayer model = data.getModel();
            double x = data.getX() - mc.getRenderManager().viewerPosX;
            double y = data.getY() - mc.getRenderManager().viewerPosY;
            y += yAnimations.getValue() * (System.currentTimeMillis() - data.getTime()) / fadeTime.getValue().doubleValue();
            double z = data.getZ() - mc.getRenderManager().viewerPosZ;

            GlStateManager.pushMatrix();
            RenderUtil.startRender();

            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180 - model.getYaw(), 0, 1, 0);
            final Color boxColor = getColor(data.getPlayer());
            final Color outlineColor = getOutlineColor(data.getPlayer());

            final float maxBoxAlpha = boxColor.getAlpha();
            final float maxOutlineAlpha = outlineColor.getAlpha();

            float alphaBoxAmount = maxBoxAlpha / this.fadeTime.getValue();
            float alphaOutlineAmount = maxOutlineAlpha / this.fadeTime.getValue();

            int fadeBoxAlpha = MathUtil.clamp((int) (alphaBoxAmount * (data.getTime() + fadeTime.getValue() - System.currentTimeMillis())), 0, (int) maxBoxAlpha);
            int fadeOutlineAlpha = MathUtil.clamp((int) (alphaOutlineAmount * (data.getTime() + fadeTime.getValue() - System.currentTimeMillis())), 0, (int) maxOutlineAlpha);

            Color box = new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), fadeBoxAlpha);
            Color out = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), fadeOutlineAlpha);

            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            double widthX = player.getEntityBoundingBox().maxX - player.getRenderBoundingBox().minX + 1;
            double widthZ = player.getEntityBoundingBox().maxZ - player.getEntityBoundingBox().minZ + 1;

            GlStateManager.scale(widthX, player.height, widthZ);

            GlStateManager.translate(0.0F, -1.501F, 0.0F);

            RenderUtil.setColor(box);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            model.render(0.0625F);

            RenderUtil.setColor(out);
            GL11.glLineWidth(lineWidth.getValue());
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            model.render(0.0625F);

            RenderUtil.endRender();
            GlStateManager.popMatrix();
        });
        popDataList.removeIf(e -> e.getTime() + fadeTime.getValue() < System.currentTimeMillis());
    }

    @EventListener
    public void onTotemPop(TotemPopEvent event) {

        if (!isValidEntity(event.getEntity()))
            return;
        EntityPlayer player = event.getEntity();

        popDataList.add(new PopData(PlayerUtil.copyPlayer(event.getEntity(), copyAnimations.getValue()),
                System.currentTimeMillis(),
                player.posX,
                player.posY,
                player.posZ
                ));
    }

//    @EventListener
//    public void onPacketReceive(PacketEvent.Receive packetEvent) {
//        if (packetEvent.getPacket() instanceof SPacketEntityStatus) {
//            SPacketEntityStatus packet = (SPacketEntityStatus) packetEvent.getPacket();
//            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
//                EntityPlayer player = (EntityPlayer) packet.getEntity(mc.world);
//                TotemPopEvent totemPopEvent = new TotemPopEvent(player);
//                EVENT_BUS.post(totemPopEvent);
//            }
//        }
//    }

    public static class PopData {
        private final EntityPlayer player;
        private final StaticModelPlayer model;
        private final long time;
        private final double x;
        private final double y;
        private final double z;

        public PopData(EntityPlayer player, long time, double x, double y, double z) {
            this.player = player;
            this.time = time;
            this.x = x;
            this.y = y - (player.isSneaking() ? 0.125 : 0);
            this.z = z;
            this.model = new StaticModelPlayer(player, player instanceof AbstractClientPlayer && ((AbstractClientPlayer) player).getSkinType().equals("slim"), 0);
            this.model.disableArmorLayers();
        }

        public EntityPlayer getPlayer() {
            return player;
        }

        public long getTime() {
            return time;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public StaticModelPlayer getModel() {
            return model;
        }
    }

}
