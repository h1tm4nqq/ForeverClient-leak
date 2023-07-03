package we.devs.forever.client.ui.alts.iasencrypt;

import net.minecraft.client.Minecraft;
import we.devs.forever.client.ui.alts.ias.account.ExtendedAccountData;
import we.devs.forever.client.ui.alts.tools.Config;
import we.devs.forever.client.ui.alts.tools.alt.AccountData;
import we.devs.forever.client.ui.alts.tools.alt.AltDatabase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;

public final class Standards {
    public static final String cfgn = ".iasx";
    public static final String pwdn = ".iasp";
    public static File IASFOLDER = Minecraft.getMinecraft().gameDir;

    public static String getPassword() {
        File passwordFile = new File(IASFOLDER, pwdn);
        if (passwordFile.exists()) {
            String pass;
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(passwordFile));
                pass = (String) stream.readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return pass;
        }
        String newPass = EncryptionTools.generatePassword();
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(passwordFile));
            out.writeObject(newPass);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Path file = passwordFile.toPath();
            DosFileAttributes attr = Files.readAttributes(file, DosFileAttributes.class);
            DosFileAttributeView view = Files.getFileAttributeView(file, DosFileAttributeView.class);
            if (!attr.isHidden()) {
                view.setHidden(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newPass;
    }

    public static void updateFolder() {

        String dir;
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) {
            dir = System.getenv("AppData");
        } else {
            dir = System.getProperty("user.home");
            if (OS.contains("MAC")) {
                dir = dir + "/Library/Application Support";
            }
        }
        IASFOLDER = new File(dir);
    }

    public static void importAccounts() {
        Standards.processData(Standards.getConfigV3());
        Standards.processData(Standards.getConfigV2());
        Standards.processData(Standards.getConfigV1(), false);
    }

    private static boolean hasData(AccountData data) {
        for (AccountData edata : AltDatabase.getInstance().getAlts()) {
            if (!edata.equalsBasic(data)) continue;
            return true;
        }
        return false;
    }

    private static void processData(Config olddata) {
        Standards.processData(olddata, true);
    }

    private static void processData(Config olddata, boolean decrypt) {
        if (olddata != null) {
            for (AccountData data : ((AltDatabase) olddata.getKey("altaccounts")).getAlts()) {
                ExtendedAccountData data2 = Standards.convertData(data, decrypt);
                if (Standards.hasData(data2)) continue;
                AltDatabase.getInstance().getAlts().add(data2);
            }
        }
    }

    private static ExtendedAccountData convertData(AccountData oldData, boolean decrypt) {
        if (decrypt) {
            if (oldData instanceof ExtendedAccountData) {
                return new ExtendedAccountData(EncryptionTools.decodeOld(oldData.user), EncryptionTools.decodeOld(oldData.pass), oldData.alias, ((ExtendedAccountData) oldData).useCount, ((ExtendedAccountData) oldData).lastused, ((ExtendedAccountData) oldData).premium);
            }
            return new ExtendedAccountData(EncryptionTools.decodeOld(oldData.user), EncryptionTools.decodeOld(oldData.pass), oldData.alias);
        }
        if (oldData instanceof ExtendedAccountData) {
            return new ExtendedAccountData(oldData.user, oldData.pass, oldData.alias, ((ExtendedAccountData) oldData).useCount, ((ExtendedAccountData) oldData).lastused, ((ExtendedAccountData) oldData).premium);
        }
        return new ExtendedAccountData(oldData.user, oldData.pass, oldData.alias);
    }

    private static Config getConfigV3() {
        File f = new File(IASFOLDER, ".ias");
        Config cfg = null;
        if (f.exists()) {
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
                cfg = (Config) stream.readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            f.delete();
        }
        return cfg;
    }

    private static Config getConfigV2() {
        File f = new File(Minecraft.getMinecraft().gameDir, ".ias");
        Config cfg = null;
        if (f.exists()) {
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
                cfg = (Config) stream.readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            f.delete();
        }
        return cfg;
    }

    private static Config getConfigV1() {
        File f = new File(Minecraft.getMinecraft().gameDir, "user.cfg");
        Config cfg = null;
        if (f.exists()) {
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
                cfg = (Config) stream.readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            f.delete();
        }
        return cfg;
    }
}

