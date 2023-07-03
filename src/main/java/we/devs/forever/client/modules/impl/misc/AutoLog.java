package we.devs.forever.client.modules.impl.misc;

import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class AutoLog extends Module {

    public static AutoLog INSTANCE;

    static {
        AutoLog.INSTANCE = new AutoLog();
    }

    public Setting<Float> health = (new Setting<>("Health", 16.0f, 0.1f, 36.0f));
    public Setting<Boolean> bed = (new Setting<>("Beds", true));
    public Setting<Float> range = (new Setting<>("BedRange", 6.0f, 0.1f, 36.0f, v -> this.bed.getValue()));
    public Setting<Boolean> logout = (new Setting<>("LogoutOff", true));

    public AutoLog() {
        super("AutoLog", "Logs when in danger.", Category.MISC);
        this.setInstance();
    }

    private void setInstance() {
        AutoLog.INSTANCE = this;
    }

    @Override
    public void onTick() {
        if (!Client.fullNullCheck() && AutoLog.mc.player.getHealth() <= this.health.getValue()) {
            moduleManager.disableModule("AutoReconnect");
            AutoLog.mc.player.connection.sendPacket(new SPacketDisconnect(new TextComponentString("AutoLogged")));
            if (this.logout.getValue()) {
                this.disable();
            }
        }
    }

    @EventListener
    public void onPacketReceive(final PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange && this.bed.getValue()) {
            final SPacketBlockChange packet = event.getPacket();
            if (packet.getBlockState().getBlock() == Blocks.BED && AutoLog.mc.player.getDistanceSqToCenter(packet.getBlockPosition()) <= MathUtil.square(this.range.getValue())) {
                moduleManager.disableModule("AutoReconnect");
                AutoLog.mc.player.connection.sendPacket(new SPacketDisconnect(new TextComponentString("AutoLogged")));
                if (this.logout.getValue()) {
                    this.disable();
                }
            }
        }
    }
}
