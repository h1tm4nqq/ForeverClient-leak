package we.devs.forever.client.modules.api.listener;
@FunctionalInterface
public interface Invoker<E> {

    void invoke(E var);
}
