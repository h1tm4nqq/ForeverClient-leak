package we.devs.forever.client.modules.impl.movement;

import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class PhaseWalk extends Module {

    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Packet));
    public Setting<Teleport> teleport = (new Setting<>("Teleport", Teleport.Full));
    public Setting<Movement> movement = (new Setting<>("Movement", Movement.Shift));
    public Setting<Float> speed = (new Setting<>("Speed",0.1f,0F,4F));
    public Setting<Float> factor = (new Setting<>("Factor",0.1f,0F,8F ));
    public Setting<Integer> delay = (new Setting<>("Delay",1,0,20));
    public Setting<Boolean> fallPacket = (new Setting<>("Fall Packet",true));
    public Setting<Boolean> teleportId = (new Setting<>("Teleport Id",true));
    public Setting<Boolean> cancelMotion = (new Setting<>("Cancel Motion",true));
    public Setting<Boolean> walkBypass = (new Setting<>("WalkBypass",true));
    public Setting<Boolean> collidedTimer = (new Setting<>("Collided Timer",false));
    public Setting<Float> timerSpeed = (new Setting<>("Timer Speed", 2F,0F,8F));

    public static float TICK_TIMER = 1;
    private int walkDelay = 0;
    private int tpId = 0;
    private TimerUtil timer = new TimerUtil();

    public PhaseWalk() {
        super("PhaseWalk","PhaseWalk",Category.MOVEMENT);

    }
    @Override
    public void onToggle(){
        timer.reset();
        walkDelay = 0;
        tpId = 0;
        mc.player.noClip = false;
    }

    public void doWalkBypas(){
        if (walkDelay >= 1){
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX,mc.player.posY,mc.player.posZ, mc.player.onGround));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX,mc.player.posY - 1,mc.player.posZ, mc.player.onGround));
        }
    }

    public void doPackets(double x, double y, double z) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, mc.player.onGround));
        switch (teleport.getValue()) {
            case Full:
                mc.player.connection.sendPacket(new CPacketPlayer.Position(x, -1337, z, mc.player.onGround));
                break;

            case Semi:
                mc.player.connection.sendPacket(new CPacketPlayer.Position(x, 0, z, mc.player.onGround));
                break;
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        walkDelay++;
        mc.player.noClip = true;

        if (movement.getValue() == Movement.Shift && !mc.gameSettings.keyBindSneak.isKeyDown()) return;

        if (cancelMotion.getValue()) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        double[] forw = EntityUtil.forward(speed.getValue() / 100);

        if (mc.player.collidedHorizontally){

            if (teleportId.getValue()) {
                tpId++;
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId - 1));
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId + 1));
            }

            if (collidedTimer.getValue())
                TICK_TIMER = timerSpeed.getValue();

            if (mode.getValue() == Mode.Packet) {
                if (timer.passedMs(delay.getValue() * 100L)) {
                    for (int i = 0; i < factor.getValue(); i++)
                        doPackets(mc.player.posX + forw[0], mc.player.posY, mc.player.posZ + forw[1]);

                    if (fallPacket.getValue())
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_RIDING_JUMP));

                    timer.reset();
                }
            }
            if (mode.getValue() == Mode.Motion) {
                mc.player.setLocationAndAngles(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch);
                for (int i = 0; i < factor.getValue(); i++) mc.player.setLocationAndAngles(mc.player.posX + forw[0], mc.player.posY, mc.player.posZ + forw[1], mc.player.rotationYaw, mc.player.rotationPitch);
            }

        } else {
            TICK_TIMER = 1;
            if (!PlayerUtil.isPlayerMoving() && walkBypass.getValue()) {
                for (int i = 0; i < 1; i++) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 1, mc.player.posZ, mc.player.onGround));
                }
            }
        }

        if (mode.getValue() == Mode.Teleport){
            for (int i = 0; i < factor.getValue(); i++) {
                doPackets(mc.player.posX + forw[0], mc.player.posY, mc.player.posZ + forw[1]);
            }
        }
    }

    @EventListener
    public void packetEventReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (!(mc.currentScreen instanceof GuiDownloadTerrain)) {
                SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                if (mc.player.isEntityAlive()) {
                    if (this.tpId <= 0) {
                        this.tpId = ((SPacketPlayerPosLook) event.getPacket()).getTeleportId();
                    }
                }
                tpId = packet.getTeleportId();
            } else tpId = 0;

        }
    }



    public enum Teleport {
        Full, Semi
    }
    public enum Mode {
        Teleport, Packet, Motion
    }
    public enum Movement {
        Shift, None
    }

}