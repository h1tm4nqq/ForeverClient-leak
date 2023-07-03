package we.devs.forever.client.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.PushEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public
class Velocity
        extends Module {
    private static Velocity INSTANCE = new Velocity();
    public Setting<Boolean> knockBack = (new Setting<>("KnockBack", true));
    public Setting<Boolean> noPush = (new Setting<>("NoPush", true));
    public Setting<Float> horizontal = (new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f));
    public Setting<Float> vertical = (new Setting<>("Vertical", 0.0f, 0.0f, 100.0f));
    public Setting<Boolean> explosions = (new Setting<>("Explosions", true));
    public Setting<Boolean> bobbers = (new Setting<>("Bobbers", true));
    public Setting<Boolean> water = (new Setting<>("Water", false));
    public Setting<Boolean> blocks = (new Setting<>("Blocks", false));

    public Velocity() {
        super("Velocity", "Allows you to control your velocity", Category.COMBAT);
        this.setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @EventListener
    public void onPacketReceived(PacketEvent.Receive event) {
        if (event.getStage() == 0 && Velocity.mc.player != null) {
            Entity entity;
            SPacketEntityStatus packet;
            SPacketEntityVelocity velocity;
            if (this.knockBack.getValue() && event.getPacket() instanceof SPacketEntityVelocity && (velocity = event.getPacket()).getEntityID() == Velocity.mc.player.entityId) {
                if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                    event.cancel();
                    return;
                }
                velocity.motionX = (int) ((float) velocity.motionX * this.horizontal.getValue());
                velocity.motionY = (int) ((float) velocity.motionY * this.vertical.getValue());
                velocity.motionZ = (int) ((float) velocity.motionZ * this.horizontal.getValue());
            }
            if (event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue() && (packet = event.getPacket()).getOpCode() == 31 && (entity = packet.getEntity(Velocity.mc.world)) instanceof EntityFishHook) {
                EntityFishHook fishHook = (EntityFishHook) entity;
                if (fishHook.caughtEntity == Velocity.mc.player) {
                    event.cancel();
                }
            }
            if (this.explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
                //velocity = (SPacketExplosion)event.getPacket();
                SPacketExplosion velocity_ = event.getPacket();
                velocity_.motionX *= this.horizontal.getValue();
                velocity_.motionY *= this.vertical.getValue();
                velocity_.motionZ *= this.horizontal.getValue();
            }
        }
    }

    @EventListener
    public void onPush(PushEvent event) {
        if (event.getStage() == 0 && this.noPush.getValue() && event.entity.equals(Velocity.mc.player)) {
            if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                event.cancel();
                return;
            }
            event.x = -event.x * (double) this.horizontal.getValue();
            event.y = -event.y * (double) this.vertical.getValue();
            event.z = -event.z * (double) this.horizontal.getValue();
        } else if (event.getStage() == 1 && this.blocks.getValue()) {
            event.cancel();
        } else if (event.getStage() == 2 && this.water.getValue() && Velocity.mc.player != null && Velocity.mc.player.equals(event.entity)) {
            event.cancel();
        }
    }
}