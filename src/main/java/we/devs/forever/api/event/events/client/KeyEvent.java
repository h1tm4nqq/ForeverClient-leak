package we.devs.forever.api.event.events.client;

import we.devs.forever.api.event.EventStage;

public
class KeyEvent extends EventStage  {
    private final boolean eventState;
    private final char character;
    private final int key;

    public KeyEvent(boolean eventState, int key, char character) {
        this.eventState = eventState;
        this.key = key;
        this.character = character;
    }

    public boolean iskeyDown()
    {
        return eventState;
    }

    public int getKey()
    {
        return key;
    }

    public char getCharacter()
    {
        return character;
    }
}
