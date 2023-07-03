/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.util.thread;

import java.util.concurrent.ExecutorService;

public interface GlobalExecutor {
    public static final ExecutorService EXECUTOR = ThreadUtil.newDaemonCachedThreadPool();
    public static final ExecutorService FIXED_EXECUTOR = ThreadUtil.newFixedThreadPool((int)((double)Runtime.getRuntime().availableProcessors() / 1.5));
}

