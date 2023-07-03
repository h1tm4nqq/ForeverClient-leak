package we.devs.forever.api.util.client;

public class ThreadUtil extends Thread {
    public Runnable runnable;

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();;
    }
}
