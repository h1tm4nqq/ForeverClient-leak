package we.devs.forever.api.event.events.client;

import we.devs.forever.api.event.EventStage;
import we.devs.forever.client.Client;
import we.devs.forever.client.setting.Setting;


public
class ClientEvent extends EventStage {

    private Client feature;
    private Setting setting;

    public ClientEvent(Client feature) {
//        super(stage);
        super();

        this.feature = feature;
    }

    public ClientEvent(Setting setting) {
        super(2);
        this.setting = setting;
    }

    public Client getFeature() {
        return this.feature;
    }

    public Setting getSetting() {
        return this.setting;
    }
}
