package we.devs.forever.api.util.render.animation;


public class TimeAnimation {

    private final double start;
    private final double end;
    private double current;
    private double progress;
    private boolean playing;
    private boolean backwards;
    private boolean reverseOnEnd = false;
    private long lastTime;
    private double per;

    public TimeAnimation(long time, double start, double end) {
        this.start = start;
        current = start;
        this.end = end;
        this.backwards = false;
        playing = true;

        per = (end - start) / time;

        lastTime = System.currentTimeMillis();
    }

    public TimeAnimation(long time, double start, double end, boolean backwards) {
        // length in ms
        this.start = start;
        current = start;
        this.end = end;
        this.backwards = backwards;
        playing = true;

        per = (end - start) / time;

        lastTime = System.currentTimeMillis();
    }


    public void update() {
        if (playing) {
            current = start + progress;
            progress += per * (System.currentTimeMillis() - lastTime);

            current = clamp(current, start, end);
            if (current >= end || (backwards && current <= start)) {
                if (reverseOnEnd) {
                    backwards = !backwards;
                    per *= -1;
                    reverseOnEnd = false;
                } else {
                    playing = false;
                }
            }
        }
        lastTime = System.currentTimeMillis();
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }


    public double getCurrent() {
        return current;
    }

    public float getCurrentFloat() {
        return (float) current;
    }

    public void reset() {
        playing = true;
        current = start;
        progress = 0;
    }

    public void setCurrent(double current) {
        this.current = current;
    }


}
