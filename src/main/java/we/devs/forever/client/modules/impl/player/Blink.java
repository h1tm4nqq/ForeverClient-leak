package we.devs.forever.client.modules.impl.player;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Blink
        extends Module {
    private final TimerUtil timer = new TimerUtil();
    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    public Setting<Boolean> render = (new Setting<>("RenderPlayer", true));
    public Setting<Boolean> cPacketPlayer = (new Setting<>("CPacketPlayer", true));
    public Setting<Boolean> stopInAir = (new Setting<>("StopInAir", false));
    public Setting<Mode> autoOff = (new Setting<>("AutoOff", Mode.Manual));
    public Setting<Integer> timeLimit = (new Setting<>("Time", 20, 1, 500, v -> autoOff.getValue() == Mode.Time));
    public Setting<Integer> packetLimit = (new Setting<>("Packets", 20, 1, 500, v -> autoOff.getValue() == Mode.Packets));
    public Setting<Float> distance = (new Setting<>("Distance", 10.0f, 1.0f, 100.0f, v -> autoOff.getValue() == Mode.Distance));
    private EntityOtherPlayerMP entity;
    private int packetsCanceled = 0;
    private BlockPos startPos = null;

    public Blink() {
        super("Blink", "Fakelag.", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck()) {
            if(render.getValue()) {
                entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
                entity.copyLocationAndAnglesFrom(mc.player);
                entity.rotationYaw = mc.player.rotationYaw;
                entity.rotationYawHead = mc.player.rotationYawHead;
                entity.inventory.copyInventory(mc.player.inventory);
                mc.world.addEntityToWorld(6942069, entity);
            }
            startPos = mc.player.getPosition();
        } else {
            disable();
        }
        packetsCanceled = 0;
        timer.reset();
    }

    @Override
    public void onUpdate() {
        if (nullCheck() || autoOff.getValue() == Mode.Time && timer.passedS(timeLimit.getValue()) || autoOff.getValue() == Mode.Distance && startPos != null && mc.player.getDistanceSq(startPos) >= distance.getValue() *distance.getValue()|| autoOff.getValue() == Mode.Packets && packetsCanceled >= packetLimit.getValue()) {
            disable();
        }
        if(stopInAir.getValue()&& (mc.player.motionY > 0.0 || !mc.player.onGround || mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.fallDistance > 2.0f)) {
            disable();
        }
    }

    @Override
    public void onLogout() {
        if (isEnabled()) {
            disable();
        }
    }

    @EventListener
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getStage() == 0 && mc.world != null && !mc.isSingleplayer()) {
            Packet<?> packet = event.getPacket();
            if (cPacketPlayer.getValue() && packet instanceof CPacketPlayer) {
                event.cancel();
                packets.add(packet);
                ++packetsCanceled;
            }
            if (!cPacketPlayer.getValue()) {
                if (packet instanceof CPacketChatMessage || packet instanceof CPacketConfirmTeleport || packet instanceof CPacketKeepAlive || packet instanceof CPacketTabComplete || packet instanceof CPacketClientStatus) {
                    return;
                }
                packets.add(packet);
                event.cancel();
                ++packetsCanceled;
            }
        }
    }

    @Override
    public void onDisable() {
        if (!fullNullCheck()) {
            if(render.getValue()) mc.world.removeEntity(entity);
            while (!packets.isEmpty()) {
                mc.player.connection.sendPacket(packets.poll());
            }
        }
        startPos = null;
    }

    public enum Mode {
        Manual,
        Time,
        Distance,
        Packets
    }
}