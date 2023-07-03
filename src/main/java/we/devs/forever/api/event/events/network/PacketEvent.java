package we.devs.forever.api.event.events.network;

import net.minecraft.network.Packet;
import we.devs.forever.api.event.EventStage;

public
class PacketEvent extends EventStage {

    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        super();
        this.packet = packet;
    }

    public <T extends Packet<?>> T getPacket() {
        return (T) this.packet;
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Receive extends PacketEvent {
        public Receive( Packet<?> packet) {
            super(packet);
        }
    }

    public static class SendPost extends PacketEvent {
        public SendPost(Packet<?> packet) {
            super(packet);
        }
    }

    public static class ReceivePost extends PacketEvent {
        public ReceivePost(Packet<?> packet) {
            super(packet);
        }
    }

}
