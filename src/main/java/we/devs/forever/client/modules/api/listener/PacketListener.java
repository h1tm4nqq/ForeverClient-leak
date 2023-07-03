//package we.devs.forever.client.modules.api.listener;
//
//import net.minecraft.network.Packet;
//import we.devs.forever.api.event.events.network.PacketEvent;
//
//public class ReceiveListener<P extends Packet<?>>
//        extends LambdaListener<PacketEvent.Receive<P>> {
//    public ReceiveListener(Class<P> target, Invoker<PacketEvent.Receive<P>> invoker) {
//        this(target, 10, invoker);
//    }
//
//    public ReceiveListener(Class<P> target, int priority, Invoker<PacketEvent.Receive<P>> invoker) {
//        super(PacketEvent.Receive.class, priority, target, invoker);
//    }
//}
