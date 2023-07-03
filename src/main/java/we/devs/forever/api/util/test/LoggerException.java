package we.devs.forever.api.util.test;

public class LoggerException {
    public static void testMethod(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
