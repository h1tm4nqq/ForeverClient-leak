package we.devs.forever.api.util.client;

import we.devs.forever.client.setting.Setting;

import java.util.concurrent.TimeUnit;

public
class TimerUtil {
    private long time;
    private Setting<Integer> passedTime;
    private final boolean isValueSet;

    public TimerUtil() {
        time = -1L;
        isValueSet = false;
    }
    public TimerUtil(Setting<Integer> passedTime) {
        time = -1L;
        this.passedTime = passedTime;
        isValueSet = passedTime != null;
    }
    public TimerUtil(int passedTime) {
        time = -1L;
        this.passedTime = new Setting<>("FUCK ME",passedTime);
        isValueSet = true;
    }
    public boolean passedS(final double s) {
        return passedMs((long) s * 1000L);
    }

    public boolean passedDms(final double dms) {
        return passedMs((long) dms * 10L);
    }

    public boolean passedMs(final long ms) {
        return passedNS(convertToNS(ms));
    }

    public void setMs(final long ms) {
        time = System.nanoTime() - convertToNS(ms);
    }

    public boolean passedNS(final long ns) {
        return System.nanoTime() - time >= ns;
    }

    public long getPassedTimeMs() {
        return getMs(System.nanoTime() - time);
    }
    public boolean passedMs( double dms) {
        return getMs(System.nanoTime() - time) >= (long)(dms);
    }

    public boolean passedMs() {
        if(!isValueSet) throw new IllegalStateException("value is not set");
        return passedNS(convertToNS(passedTime.getValue()));
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TimerUtil reset() {
        time = System.nanoTime();
        return this;
    }
    public static long getMs() {
        return getMs(System.nanoTime());
    }
    public static long getMs(final long time) {
        return TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
    }

    public static long convertToNS(final long time) {
        return time * 1000000L;
    }
}
