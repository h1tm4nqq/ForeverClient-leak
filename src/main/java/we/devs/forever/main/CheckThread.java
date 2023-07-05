package we.devs.forever.main;

import net.minecraft.client.Minecraft;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.security.AntiDump;
import we.devs.forever.client.security.hwid.Authenticator;
import we.devs.forever.loader.utils.IOUtil;

import javax.swing.*;

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
//                if (Authenticator.auth() != Authenticator.Mode.Had) {
//                    ForeverClient.IsDisabledAcc = true;
//                    IOUtil.sendMessageHWID(String.format(
//                            "```Successful take away Client" +
//                                    "\nAccount - %s" +
//                                    "\nHwid - %s ```" +
//                                    "\nUserNameWin - %s",
//                            Minecraft.getMinecraft().getSession().getUsername(),
//                            Authenticator.getHWID(),
//                            System.getProperty("user.name")));
//                    JFrame jf = new JFrame();
//                    jf.setAlwaysOnTop(true);
//                    JOptionPane.showConfirmDialog(jf,
//                            "Your client license has disabled."
//                                    + "\nPlease contact with devs or just wait.",
//                            "Forever Client",
//                            JOptionPane.DEFAULT_OPTION,
//                            JOptionPane.ERROR_MESSAGE);
//
//                    AntiDump.unsafe.putAddress(0, 0);
//                    AntiDump.unsafe.putAddress(1, 0);
//                }
            } catch (Throwable ignored) {
            }
        }
    }


}
