package we.devs.forever.api.manager.impl.client;

import we.devs.forever.api.manager.api.AbstractManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MuteManager extends AbstractManager {

    private static final Set<String> muted = new HashSet<>();

    public MuteManager() {
        super("MuteManager");
    }

    public static boolean isMuted(String name) {
        for(String string : muted) {
            if(string.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static void addMuted(String add) {
        muted.add(add);
    }

    public static void removeMuted(String remove) {
        muted.remove(remove);
    }

    public static void clearMuted() {
        muted.clear();
    }

    public static Set<String> getMuted() {
        return muted;
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}
