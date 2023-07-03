/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.util.thread;

import we.devs.forever.api.util.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ThreadUtil
implements Util {
    public static final ThreadFactory FACTORY = ThreadUtil.newDaemonThreadFactoryBuilder().setNameFormat("ForeverClient-Thread-%d").build();

    public static ScheduledExecutorService newDaemonScheduledExecutor(String name) {
        ThreadFactoryBuilder factory = ThreadUtil.newDaemonThreadFactoryBuilder();
        factory.setNameFormat("ForeverClient-" + name + "-%d");
        return Executors.newSingleThreadScheduledExecutor(factory.build());
    }

    public static ExecutorService newDaemonCachedThreadPool() {
        return Executors.newCachedThreadPool(FACTORY);
    }

    public static ExecutorService newFixedThreadPool(int size) {
        ThreadFactoryBuilder factory = ThreadUtil.newDaemonThreadFactoryBuilder();
        factory.setNameFormat("ForeverClient-Fixed-%d");
        return Executors.newFixedThreadPool(Math.max(size, 1), factory.build());
    }

    public static ThreadFactoryBuilder newDaemonThreadFactoryBuilder() {
        ThreadFactoryBuilder factory = new ThreadFactoryBuilder();
        factory.setDaemon(true);
        return factory;
    }
}

