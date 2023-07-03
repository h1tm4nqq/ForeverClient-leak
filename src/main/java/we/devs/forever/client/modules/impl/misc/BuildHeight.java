package we.devs.forever.client.modules.impl.misc;

import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class BuildHeight extends Module {
    public Setting<Integer> height = (new Setting<>("Height", 255, 0, 255));

    public BuildHeight() {
        super("BuildHeight", "Allows you to place/interact at build height.", Category.MISC);
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        CPacketPlayerTryUseItemOnBlock packet;
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && (packet = event.getPacket()).getPos().getY() >= this.height.getValue() && packet.getDirection() == EnumFacing.UP) {
            packet.placedBlockDirection = EnumFacing.DOWN;
        }
    }
}