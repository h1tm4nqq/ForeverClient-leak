package we.devs.forever.client.ui.alts.tools;

import net.minecraft.client.Minecraft;
import we.devs.forever.client.ui.alts.iasencrypt.Standards;
import we.devs.forever.client.ui.alts.tools.alt.AltDatabase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;

public class Config
        implements Serializable {
    public static final long serialVersionUID = -559038737L;
    private static final String configFileName = ".iasx";
    private static Config instance;
    private final ArrayList<Pair<String, Object>> field_218893_c = new ArrayList();

    private Config() {
        instance = this;
    }

    public static Config getInstance() {
        return instance;
    }

    public static void save() {
        Config.saveToFile();
    }

    public static void load() {
        Config.loadFromOld();
        Config.readFromFile();
    }

    private static void readFromFile() {
        File f = new File(Standards.IASFOLDER, configFileName);
        if (f.exists()) {
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
                instance = (Config) stream.readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                instance = new Config();
                f.delete();
            }
        }
        if (instance == null) {
            instance = new Config();
        }
    }

    private static void saveToFile() {
        DosFileAttributeView view;
        DosFileAttributes attr;
        Path file2;
        try {
            file2 = new File(Standards.IASFOLDER, configFileName).toPath();
            attr = Files.readAttributes(file2, DosFileAttributes.class);
            view = Files.getFileAttributeView(file2, DosFileAttributeView.class);
            if (attr.isHidden()) {
                view.setHidden(false);
            }
        } catch (NoSuchFileException file3) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(Standards.IASFOLDER, configFileName)));
            out.writeObject(instance);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            file2 = new File(Standards.IASFOLDER, configFileName).toPath();
            attr = Files.readAttributes(file2, DosFileAttributes.class);
            view = Files.getFileAttributeView(file2, DosFileAttributeView.class);
            if (!attr.isHidden()) {
                view.setHidden(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFromOld() {
        File f = new File(Minecraft.getMinecraft().gameDir, "user.cfg");
        if (f.exists()) {
            try {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
                instance = (Config) stream.readObject();
                stream.close();
                f.delete();
                System.out.println("Loaded data from old file");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                f.delete();
            }
        }
    }

    public void setKey(Pair<String, Object> key) {
        if (this.getKey(key.getFirst()) != null) {
            this.removeKey(key.getFirst());
        }
        this.field_218893_c.add(key);
        Config.save();
    }

    public void setKey(String key, AltDatabase value) {
        this.setKey(new Pair<String, Object>(key, value));
    }

    public Object getKey(String key) {
        for (Pair<String, Object> aField_218893_c : this.field_218893_c) {
            if (!aField_218893_c.getFirst().equals(key)) continue;
            return aField_218893_c.getSecond();
        }
        return null;
    }

    private void removeKey(String key) {
        for (int i = 0; i < this.field_218893_c.size(); ++i) {
            if (!this.field_218893_c.get(i).getFirst().equals(key)) continue;
            this.field_218893_c.remove(i);
        }
    }
}

