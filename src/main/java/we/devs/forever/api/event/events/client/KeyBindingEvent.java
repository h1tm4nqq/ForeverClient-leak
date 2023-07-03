package we.devs.forever.api.event.events.client;

import we.devs.forever.api.event.EventStage;

public class KeyBindingEvent extends EventStage {

    public boolean holding;
    public boolean pressed;

    public KeyBindingEvent(boolean holding, boolean pressed) {
        super();
        this.holding = holding;
        this.pressed = pressed;
    }

    public boolean isHolding() {
        return holding;
    }

    public boolean isPressed() {
        return pressed;
    }
}
