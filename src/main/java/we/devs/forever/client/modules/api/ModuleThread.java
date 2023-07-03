package we.devs.forever.client.modules.api;

import we.devs.forever.client.Client;

import java.util.concurrent.ThreadLocalRandom;

public abstract class ModuleThread<M extends Module> extends Client {

    protected final M module;
    @SuppressWarnings("all")
    private Thread thread = new Thread();
    private final String name;
    private  long delay;

    public ModuleThread(M module, long delay) {
        this.module = module;
        this.delay = delay;
        name = "ForeverClient Thread - " + ThreadLocalRandom.current().nextInt();
    }

    public ModuleThread(M module, String name, long delay) {
        this.module = module;
        this.name = name;
        this.delay = delay;
    }

    public abstract void invoke();
    private void invoke0() {
        try {
            while (!getThread().isInterrupted()) {
                Thread.sleep(delay);
                try {
                    invoke();
                }  catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

            }
        }
        catch (InterruptedException ignored) {}

    }

    public final void start() {
            try {
                thread.interrupt();
                thread = new Thread(this::invoke0, name);
                thread.setDaemon(true);
                thread.start();
            } catch (Throwable t) {
                t.printStackTrace();
        }
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    public final void join() throws InterruptedException {
        thread.join();

    }

    public final Thread getThread() {
        return thread;
    }
}
