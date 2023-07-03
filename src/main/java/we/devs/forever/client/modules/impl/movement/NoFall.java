package we.devs.forever.client.modules.impl.movement;

import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.events.player.PlayerUpdateEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.exploit.packetfly.PacketFly;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.mixin.mixins.accessor.ICPacketPlayer;
import we.devs.forever.mixin.mixins.accessor.ISPacketPlayerPosLook;

import java.util.ArrayList;

public class NoFall extends Module {


    private final Setting<Integer> fallDistance = (new Setting<>("FallDistance", 3, 1, 100));

    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Packet));

    private double lowestY = 256;

    private int teleportId;
    private final ArrayList<CPacketPlayer> packets = new ArrayList<>();

    private enum Mode {
        PacketFly, Anti, Packet, TP
    }

    public NoFall() {
        super("NoFall", "NoFallDamage", Category.MOVEMENT);
    }

    @EventListener
    public void onUpdate(PlayerUpdateEvent event) {
        //Retard check
        if (mc.player == null || mc.world == null) {
            return;
        }
        switch (mode.getValue()) {
            case PacketFly:
                if (mc.player.fallDistance > fallDistance.getValue()) {
                    if (teleportId <= 0) {
                        // sending this without any other packets will probs cause server to send SPacketPlayerPosLook to fix our pos
                        CPacketPlayer boundsPos = new CPacketPlayer.Position(PacketFly.randomHorizontal(), 1, PacketFly.randomHorizontal(), mc.player.onGround);
                        packets.add(boundsPos);
                        mc.player.connection.sendPacket(boundsPos);
                    } else {
                        CPacketPlayer nextPos = new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.062, mc.player.posZ, mc.player.onGround);
                        packets.add(nextPos);
                        mc.player.connection.sendPacket(nextPos);
                        CPacketPlayer downPacket = new CPacketPlayer.Position(mc.player.posX, 1, mc.player.posZ, mc.player.onGround);
                        packets.add(downPacket);
                        mc.player.connection.sendPacket(downPacket);
                        teleportId++;
                        mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportId - 1));
                        mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportId));
                        mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportId + 1));
                    }
                }
                break;
            case TP:
                if (mc.player.fallDistance >= fallDistance.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 10000, mc.player.posZ, mc.player.onGround));
                }
                break;
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer) {
            switch (mode.getValue()) {
                case Anti:
                    if (mc.player.fallDistance > Math.min(3.0F, fallDistance.getValue())) { // only works up to 3
                        if (((CPacketPlayer) event.getPacket()).getY(mc.player.posY) < lowestY) {
                            ((ICPacketPlayer) event.getPacket()).setY(mc.player.posY + Math.min(3.0F, fallDistance.getValue()));
                            lowestY = ((CPacketPlayer) event.getPacket()).getY(mc.player.posY);
                        } else {
                            lowestY = 256;
                            mc.player.fallDistance = 0;
                        }
                    }
                    break;
                case Packet:
                    if (mc.player.fallDistance > fallDistance.getValue()) {
                        ((ICPacketPlayer) event.getPacket()).setOnGround(true);
                    }
                    break;
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mode.getValue() == Mode.PacketFly
                && mc.player.fallDistance > fallDistance.getValue()
                && event.getPacket() instanceof SPacketPlayerPosLook) {
            if (!(mc.currentScreen instanceof GuiDownloadTerrain)) {
                if (mc.player.isEntityAlive()) {
                    if (this.teleportId <= 0) {
                        this.teleportId = ((SPacketPlayerPosLook) event.getPacket()).getTeleportId();
                    } else {
                        SPacketPlayerPosLook packet = event.getPacket();
                        ((ISPacketPlayerPosLook) packet).setYaw(mc.player.rotationYaw);
                        ((ISPacketPlayerPosLook) packet).setPitch(mc.player.rotationPitch);
                    }
                }
            } else {
                teleportId = 0;
            }
        }
    }

    @EventListener
    public void onMove(MoveEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mode.getValue() == Mode.PacketFly
                && mc.player.fallDistance > fallDistance.getValue() && !mc.player.onGround) {
            event.setX(0);
            event.setY(-0.062);
            event.setZ(0);
        }
    }
    @EventListener
    public void onEnable() {
        lowestY = 256;
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }
}