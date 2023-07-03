package we.devs.forever.client.modules.impl.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.util.MovementInput;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class EntitySpeed extends Module {

    public Setting<Double> speed = (new Setting<>("Speed", 1.0, 0.1, 5.0));

    public EntitySpeed() {
        super("EntitySpeed", "Custom entity speed", Category.MOVEMENT);
    }

    private static void speedEntity(Entity entity, Double speed) {
        if (entity instanceof EntityLlama) {
            entity.rotationYaw = EntitySpeed.mc.player.rotationYaw;
            ((EntityLlama) entity).rotationYawHead = EntitySpeed.mc.player.rotationYawHead;
        }
        MovementInput movementInput = EntitySpeed.mc.player.movementInput;
        double forward = movementInput.moveForward;
        double strafe = movementInput.moveStrafe;
        float yaw = EntitySpeed.mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            entity.motionX = 0.0;
            entity.motionZ = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float) (forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float) (forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            entity.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
            entity.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
            if (entity instanceof EntityMinecart) {
                EntityMinecart em = (EntityMinecart) entity;
                em.setVelocity(forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)), em.motionY, forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
            }
        }
    }

    @Override
    public void onUpdate() {
        try {
            if (EntitySpeed.mc.player.getRidingEntity() != null) {
                Entity theEntity = EntitySpeed.mc.player.getRidingEntity();
                EntitySpeed.speedEntity(theEntity, this.speed.getValue());
            }
        } catch (Exception exception) {
            // empty catch block
        }
    }
}