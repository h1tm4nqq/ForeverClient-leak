package we.devs.forever.client.modules.impl.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Trails extends Module {
    private final Setting<Float> thick = (new Setting<>("LineWidth", 1.5f, 0.1f, 5.0f));
    private final Setting<Boolean> pearl = (new Setting<>("Pearl", true));
    private final Setting<Boolean> arrows = (new Setting<>("Arrows", true));
    private final Setting<Boolean> exp = (new Setting<>("Experience Bottles", true));
    private final Setting<Boolean> potions = (new Setting<>("Splash Potions", true));
    private final Setting<Boolean> render = (new Setting<>("Render", true));
    private final Setting<Double> aliveTime = (new Setting<>("Fade Time", 5.0, 0.0, 20.0));
    private final Setting<Integer> rDelay = (new Setting<>("Delay Before Render", 120, 0, 360));
    private final Setting<Color> color = (new Setting<>("Color", new Color(30, 167, 255, 255), ColorPickerButton.Mode.Normal, 100));
    private final HashMap<UUID, List<Vec3d>> poses = new HashMap<>();
    private final HashMap<UUID, Double> time = new HashMap<>();

    public Trails() {
        super("Trails", "Draws a line behind projectiles", Category.RENDER);
    }

    public void onUpdate() {
        List<Vec3d> v;
        UUID toRemove = null;
        for (UUID uuid : this.time.keySet()) {
            if (this.time.get(uuid) <= 0.0) {
                this.poses.remove(uuid);
                toRemove = uuid;
                continue;
            }
            this.time.replace(uuid, this.time.get(uuid) - 0.05);
        }
        if (toRemove != null) {
            this.time.remove(toRemove);
            toRemove = null;
        }
        if ((this.arrows.getValue()) || (this.exp.getValue()) || (this.pearl.getValue()) || (this.potions.getValue())) {
            for (Entity e : Trails.mc.world.getLoadedEntityList()) {
                if (!(e instanceof EntityArrow) && !(e instanceof EntityExpBottle) && !(e instanceof EntityPotion)) continue;
                if (!this.poses.containsKey(e.getUniqueID())) {
                    this.poses.put(e.getUniqueID(), new ArrayList<>(Collections.singletonList(e.getPositionVector())));
                    this.time.put(e.getUniqueID(), 0.05);
                    continue;
                }
                this.time.replace(e.getUniqueID(), 0.05);
                v = this.poses.get(e.getUniqueID());
                v.add(e.getPositionVector());
            }
        }
        for (Entity e : Trails.mc.world.getLoadedEntityList()) {
            if (!(e instanceof EntityEnderPearl)) continue;
            if (!this.poses.containsKey(e.getUniqueID())) {
                this.poses.put(e.getUniqueID(), new ArrayList<>(Collections.singletonList(e.getPositionVector())));
                this.time.put(e.getUniqueID(), this.aliveTime.getValue());
                continue;
            }
            this.time.replace(e.getUniqueID(), this.aliveTime.getValue());
            v = this.poses.get(e.getUniqueID());
            v.add(e.getPositionVector());
        }
    }

    public void onRender3D(Render3DEvent event) {
        if (!(this.render.getValue()) && !this.poses.isEmpty()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth((this.thick.getValue()));
        Iterator<UUID> iterator = this.poses.keySet().iterator();
        while (true) {
            if (!iterator.hasNext()) {
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
                GL11.glPopMatrix();
                return;
            }
            UUID uuid = iterator.next();
            if (this.poses.get(uuid).size() <= 2) continue;
            int delay = 0;
            GL11.glBegin(1);
            for (int i = 1; i < this.poses.get(uuid).size(); delay += (this.rDelay.getValue()), ++i) {
                GL11.glColor4d(((this.color.getColor().getRed()) / 255.0f), ((this.color.getColor().getGreen()) / 255.0f), ((this.color.getColor().getBlue()) / 255.0f), ((this.color.getColor().getAlpha()) / 255.0f));
                List<Vec3d> pos = this.poses.get(uuid);
                GL11.glVertex3d((pos.get(i).x - Trails.mc.getRenderManager().viewerPosX), (pos.get(i).y - Trails.mc.getRenderManager().viewerPosY), (pos.get(i).z - Trails.mc.getRenderManager().viewerPosZ));
                GL11.glVertex3d((pos.get((i - 1)).x - Trails.mc.getRenderManager().viewerPosX), (pos.get((i - 1)).y - Trails.mc.getRenderManager().viewerPosY), (pos.get((i - 1)).z - Trails.mc.getRenderManager().viewerPosZ));
            }
            GL11.glEnd();
        }
    }
}