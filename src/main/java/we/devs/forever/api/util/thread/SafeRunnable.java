/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.util.thread;

@FunctionalInterface
public interface SafeRunnable
extends Runnable {
    public void runSafely() throws Throwable;

    @Override
    default void run() {
        try {
            this.runSafely();
        }
        catch (Throwable t) {
            this.handle(t);
        }
    }

    default void handle(Throwable t) {
        t.printStackTrace();
    }
}

