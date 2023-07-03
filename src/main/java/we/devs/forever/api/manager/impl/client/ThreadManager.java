/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.manager.impl.client;

import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.thread.GlobalExecutor;
import we.devs.forever.api.util.thread.SafeRunnable;

import java.util.concurrent.Future;

public class ThreadManager extends AbstractManager
        implements GlobalExecutor {
    private final ClientService clientService = new ClientService();
    public ThreadManager() {
        super("Thread manager");
    }

    public Future<?> submit(SafeRunnable runnable) {
        return this.submitRunnable(runnable);
    }

    public Future<?> submitRunnable(Runnable runnable) {
        return EXECUTOR.submit(runnable);
    }

    public void shutDown() {
        EXECUTOR.shutdown();
    }

    public static class ClientService extends Thread implements Util {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5);
                    // check if the mc world is running
                    if (!nullCheck()) {


                        // module onThread
                        moduleManager.onThread();

                        // manager onThread
//                        for (Manager manager : getCosmos().getAllManagers()) {
//
//                            // check if the manager is safe to run
//                            if (nullCheck() || getCosmos().getNullSafeFeatures().contains(manager)) {
//
//                                // run
//                                try {
//                                    manager.onThread();
//                                } catch (Exception exception) {
//
//                                    // print stacktrace if in dev environment
//                                    if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
//                                        exception.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
//                    }
                    }

                    // give up thread resources
                    else {
                        Thread.yield();
                    }

                } catch (Throwable exception) {
//                        exception.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onLoad() {
        clientService.setName("forever-client-thread");
        clientService.setDaemon(true);
        clientService.start();
    }

    @Override
    protected void onUnload() {
        shutDown();
    }
}

