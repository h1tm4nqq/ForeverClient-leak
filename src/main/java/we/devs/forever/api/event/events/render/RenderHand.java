package we.devs.forever.api.event.events.render;


import we.devs.forever.api.event.EventStage;

public class RenderHand extends EventStage {
    private final float ticks;

    public RenderHand(final float ticks) {
        this.ticks = ticks;
    }

    public float getPartialTicks() {
        return this.ticks;
    }

    public static class PostOutline extends RenderHand {
        public PostOutline(final float ticks) {
            super(ticks);
        }
    }

    public static class PreOutline extends RenderHand {
        public PreOutline(final float ticks) {
            super(ticks);
        }
    }

    public static class PostFill extends RenderHand {
        public PostFill(final float ticks) {
            super(ticks);
        }
    }

    public static class PreFill extends RenderHand {
        public PreFill(final float ticks) {
            super(ticks);
        }
    }

    public static class PostBoth extends RenderHand {
        public PostBoth(final float ticks) {
            super(ticks);
        }
    }

    public static class PreBoth extends RenderHand {
        public PreBoth(final float ticks) {
            super(ticks);
        }
    }
}
