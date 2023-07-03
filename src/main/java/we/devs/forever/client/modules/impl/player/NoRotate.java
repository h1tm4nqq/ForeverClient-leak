package we.devs.forever.client.modules.impl.player;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class NoRotate extends Module {
    private final Setting<Integer> waitDelay;
    private final TimerUtil timerUtil;
    private boolean cancelPackets;
    private boolean timerReset;

    public NoRotate() {
        super("NoRotate", "Dangerous to use might desync you.", Category.PLAYER);
        this.waitDelay = (Setting<Integer>) (new Setting<>("Delay", 2500, 0, 10000));
        this.timerUtil = new TimerUtil();
        this.cancelPackets = true;
        this.timerReset = false;
    }

    @Override
    public void onLogout() {
        this.cancelPackets = false;
    }

    @Override
    public void onLogin() {
        this.timerUtil.reset();
        this.timerReset = true;
    }

    @Override
    public void onUpdate() {
        if (this.timerReset && !this.cancelPackets && this.timerUtil.passedMs(this.waitDelay.getValue())) {
            this.cancelPackets = true;
            this.timerReset = false;
        }
    }


    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!fullNullCheck() && event.getStage() == 0 && cancelPackets && event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            if (packet != null) {
                packet.yaw = mc.player.rotationYaw;
                packet.pitch = mc.player.rotationPitch;
            }

        }
    }
}
