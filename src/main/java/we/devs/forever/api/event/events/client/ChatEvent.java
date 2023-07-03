package we.devs.forever.api.event.events.client;
/*
 * @author Crystallinqq on 6/29/2020
 */

import we.devs.forever.api.event.EventStage;


public
class ChatEvent extends EventStage {
    private final String msg;

    public ChatEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return this.msg;
    }
}
