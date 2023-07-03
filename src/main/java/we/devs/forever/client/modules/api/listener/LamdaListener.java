package we.devs.forever.client.modules.api.listener;

public class LamdaListener<E> extends Listener<E>{
    private final Invoker<E> invoker;
    public LamdaListener(Class<E> event,Invoker<E> invoker) {
        super(event);
        this.invoker = invoker;
    }

    @Override
    public void invoke(E event) {
        invoker.invoke(event);
    }
}
