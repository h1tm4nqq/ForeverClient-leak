package we.devs.forever.client.modules.impl.player;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.PushEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

//TODO: legit mode that doesnt cancel packets but changes them to legit packets
public
class Freecam extends Module {
    public static Freecam INSTANCE;

    public Setting<Double> speed = (new Setting<>("Speed", 0.5, 0.1, 5.0)); //Weird rounding issues when this was a float...
    public Setting<Boolean> view = (new Setting<>("3D", false));
    public Setting<Boolean> packet = (new Setting<>("Packet", true));
    public Setting<Boolean> disable = (new Setting<>("Logout/Off", true));
    private AxisAlignedBB oldBoundingBox;
    public EntityOtherPlayerMP entity;
    private Vec3d position;
    private Entity riding;
    private float yaw;
    private float pitch;

    public Freecam() {
        super("Freecam", "Look around freely.", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck()) {
            oldBoundingBox = mc.player.getEntityBoundingBox();
            mc.player.setEntityBoundingBox(new AxisAlignedBB(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.posX, mc.player.posY, mc.player.posZ));
            if (mc.player.getRidingEntity() != null) {
                riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
            }
            entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            entity.copyLocationAndAnglesFrom(mc.player);
            entity.rotationYaw = mc.player.rotationYaw;
            entity.rotationYawHead = mc.player.rotationYawHead;
            entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(69420, entity);
            position = mc.player.getPositionVector();
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            mc.player.noClip = true;
        }
    }

    @Override
    public void onDisable() {
        if (!fullNullCheck()) {
            mc.player.setEntityBoundingBox(oldBoundingBox);
            if (riding != null) {
                mc.player.startRiding(riding, true);
            }
            if (entity != null) {
                mc.world.removeEntity(entity);
            }
            if (position != null) {
                mc.player.setPosition(position.x, position.y, position.z);
            }
            mc.player.rotationYaw = yaw;
            mc.player.rotationPitch = pitch;
            mc.player.noClip = false;
        }
    }

    @Override
    public void onUpdate() {
        mc.player.noClip = true;
        mc.player.setVelocity(0, 0, 0);
        mc.player.jumpMovementFactor = speed.getValue().floatValue();
        double[] dir = MathUtil.directionSpeed(speed.getValue());
        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];
        } else {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }
        mc.player.setSprinting(false);
        if (view.getValue() && !(mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindJump.isKeyDown())) {
            mc.player.motionY = (speed.getValue() * (-MathUtil.degToRad(mc.player.rotationPitch))) * mc.player.movementInput.moveForward;
        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionY += speed.getValue();
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            mc.player.motionY -= speed.getValue();
        }
    }

    @Override
    public void onLogout() {
        if (disable.getValue()) {
            this.disable();
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && (event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput)) {
            event.cancel();
        }
    }

    @EventListener
    public void onPush(PushEvent event) {
        if (event.getStage() == 1) {
            event.cancel();
        }
    }
}
