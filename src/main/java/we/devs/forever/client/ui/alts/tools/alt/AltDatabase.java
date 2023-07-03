package we.devs.forever.client.ui.alts.tools.alt;

import we.devs.forever.client.ui.alts.tools.Config;

import java.io.Serializable;
import java.util.ArrayList;

public class AltDatabase
        implements Serializable {
    public static final long serialVersionUID = -1585600597L;
    private static AltDatabase instance;
    private final ArrayList<AccountData> altList = new ArrayList();

    private AltDatabase() {
    }

    private static void loadFromConfig() {
        if (instance == null) {
            instance = (AltDatabase) Config.getInstance().getKey("altaccounts");
        }
    }

    private static void saveToConfig() {
        Config.getInstance().setKey("altaccounts", instance);
    }

    public static AltDatabase getInstance() {
        AltDatabase.loadFromConfig();
        if (instance == null) {
            instance = new AltDatabase();
            AltDatabase.saveToConfig();
        }
        return instance;
    }

    public ArrayList<AccountData> getAlts() {
        return this.altList;
    }
}

