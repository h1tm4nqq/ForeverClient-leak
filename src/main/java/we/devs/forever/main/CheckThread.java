package we.devs.forever.main;

import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;

public class CheckThread extends Thread {
    @Override
    public void run() {
        while (!interrupted()) {
            try {
                Thread.sleep(60000);
                if(AutoCrystal.getInstance().isDisabled()){
                    if(!AutoCrystal.getInstance().calcThread.getThread().isInterrupted()){
                        AutoCrystal.getInstance().calcThread.getThread().interrupt();
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }


}
