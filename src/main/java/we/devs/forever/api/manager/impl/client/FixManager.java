package we.devs.forever.api.manager.impl.client;

import we.devs.forever.api.manager.api.AbstractManager;

public class FixManager extends AbstractManager {
    private static boolean doFix = false;

    public Object getObjectToSwap() {
        return objectToSwap;
    }

    public void setObjectToSwap(Object objectToSwap) {
        FixManager.objectToSwap = objectToSwap;
    }

    protected static Object objectToSwap;

    public FixManager() {
        super("FixManager");
    }

    public boolean isNeedFix() {
        return doFix;
    }

    public void enableFix() {
        doFix = true;
    }

    public void disableFix() {
        doFix = false;
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}
