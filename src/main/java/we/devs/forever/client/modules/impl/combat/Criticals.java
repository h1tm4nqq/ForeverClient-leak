package we.devs.forever.client.modules.impl.combat;

import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class Criticals extends Module {

    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Packet));
    public Setting<Boolean> webCrits = (new Setting<>("WebCrits", true, v -> mode.getValue() == Mode.Bypass));
    public Setting<Boolean> vehicles = (new Setting<>("Vehicles", true));
    public Setting<Integer> hits = (new Setting<>("Hits", 3, 0, 15, v -> vehicles.getValue()));
    private final Setting<Integer> delay = new Setting<>("Delay", 1, 1, 10);
    public Setting<Boolean> onlyWhenKA = (new Setting<>("OnlyWhenKA", true));
    public Criticals() {
        super("Criticals", "Scores criticals for you", Category.COMBAT);
    }

    private final Queue<CPacketUseEntity> vehicleHitQueue = new LinkedList<>();

    private CPacketUseEntity delayedPacket = null;
    private CPacketAnimation delayedAnimation = null;
    private int awaitingPackets = 0;
    int tick = 0;

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!vehicleHitQueue.isEmpty() && tick % delay.getValue() == 0) {
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            mc.player.connection.sendPacket(Objects.requireNonNull(vehicleHitQueue.poll()));
        }
        tick++;
        if (mc.player.motionY < 0 && delayedPacket != null && delayedAnimation != null && (mode.getValue() == Mode.Jump || mode.getValue() == Mode.SmallJump)) {
            mc.player.connection.sendPacket(delayedPacket);
            mc.player.connection.sendPacket(delayedAnimation);
            delayedPacket = null;
            delayedAnimation = null;
        }
    }

    @Override
    public void onEnable() {
        delayedPacket = null;
        delayedAnimation = null;
    }


    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (!Aura.INSTANCE.isEnabled() && onlyWhenKA.getValue()) return;
        if (mode.getValue() == Mode.Jump || mode.getValue() == Mode.SmallJump) {
            if (delayedPacket != null && delayedAnimation != null) {
                return;
            }
        }
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && mc.player.onGround && mc.player.collidedVertically && !mc.player.isInLava() && !mc.player.isInWater()) {
            Entity attackedEntity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world);
            if (attackedEntity instanceof EntityEnderCrystal || attackedEntity == null) return;

            if ((attackedEntity instanceof EntityMinecart || attackedEntity instanceof EntityBoat) && vehicles.getValue()) {
                if (awaitingPackets > 0) {
                    awaitingPackets--;
                    return;
                }
                awaitingPackets = hits.getValue();
                for (int i = 0; i < hits.getValue(); i++) {
                    vehicleHitQueue.add(new CPacketUseEntity(attackedEntity));
                }
                return;
            }

            switch (mode.getValue()) {
                case Packet:
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                case Bypass:
                    if (webCrits.getValue()) {
                        if (mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockWeb) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                            break;
                        }
                    }
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579D, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0000013579D, mc.player.posZ, false));
                    break;
                case Jump:
                    if (delayedPacket == null) {
                        mc.player.jump();
                        delayedPacket = event.getPacket();
                        event.setCanceled(true);
                    }
                    break;
                case SmallJump:
                    if (delayedPacket == null) {
                        mc.player.jump();
                        mc.player.motionY = 0.25;
                        delayedPacket = event.getPacket();
                        event.setCanceled(true);
                    }
                    break;
            }
        } else if (event.getPacket() instanceof CPacketAnimation && mc.player.onGround && mc.player.collidedVertically && !mc.player.isInLava() && !mc.player.isInWater() && delayedPacket != null && delayedAnimation == null) {
            delayedAnimation = event.getPacket();
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    public enum Mode {
        Packet, Bypass, Jump, SmallJump
    }
}
