package we.devs.forever.api.manager.impl.config;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import we.devs.forever.api.manager.impl.client.FriendManager;
import we.devs.forever.api.manager.impl.client.ModuleManager;
import we.devs.forever.api.manager.impl.client.MuteManager;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.EnumConverter;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.main.ForeverClient;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final File ForeverFolder = new File(mc.gameDir, "Forever");
    public static final File CONFIGS = new File(ForeverFolder, "configs");
    public static final File UtilFolder = new File(ForeverFolder, "util");
    public static final File Friends = new File(ForeverFolder, "Friends.json");
    public static final File Mutes = new File(UtilFolder, "Mutes.json");
    public static final File FONTS_FOLDER = new File(ConfigManager.ForeverFolder, "fonts");
    public static File currentConfig = new File(CONFIGS, "default.json");
    static boolean flag = false;

    public static void init() {
        if (!ForeverFolder.exists()) ForeverFolder.mkdir();
        if (!UtilFolder.exists()) UtilFolder.mkdir();
        if (!CONFIGS.exists()) CONFIGS.mkdir();
        if (!FONTS_FOLDER.exists()) FONTS_FOLDER.mkdir();

        try {

            if (!Friends.exists()) Friends.createNewFile();
            if (!Mutes.exists()) Mutes.createNewFile();

        } catch (IOException ignored) {}

        if (!currentConfig.exists()) {
            try {
                currentConfig.createNewFile();
            } catch (IOException ignored) {
            }
        }
        load(new File(CONFIGS, loadCurrentConfig()));
        loadFriends();
        loadMutes();
        ForeverClient.LOGGER.info("Config loaded.");
    }

    public static void saveCurrentConfig() {
        File currentConfig0 = new File("Forever/CurrentConfig.json");
        try {
            if (currentConfig0.exists()) {
                FileWriter writer = new FileWriter(currentConfig0);
                writer.write(currentConfig.getName());
                writer.close();
            } else {
                if (currentConfig0.createNewFile()) {
                    FileWriter writer = new FileWriter(currentConfig0);
                    writer.write(currentConfig.getName());
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadCurrentConfig() {
        File currentConfig = new File("Forever/CurrentConfig.json");
        String name = "default.json";
        try {
            if (currentConfig.exists()) {
                Scanner reader = new Scanner(currentConfig);
                while (reader.hasNextLine()) {
                    name = reader.nextLine();
                }
                reader.close();
                if (!new File(CONFIGS, name).exists()) {
                    name = "default.json";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    private static void loadFriends() {
        try {
            FileReader reader = new FileReader(Friends);
            JsonParser parser = new JsonParser();

            JsonObject friends = null;
            try {
                friends = (JsonObject) parser.parse(reader);
            } catch (ClassCastException e) {
                saveFriends();
            }
            JsonArray friendss = null;
            try {
                friendss = friends.getAsJsonArray("Friends");
            } catch (Exception e) {
                System.err.println("Friend Array not found, skipping!");
            }

            if (friendss != null) {
                Client.friendManager.cleanFriends();
                friendss.forEach(f -> parseFriend(f.getAsJsonObject()));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    private static void loadMutes() {
        try {
            FileReader reader = new FileReader(Mutes);
            JsonParser parser = new JsonParser();

            JsonObject friends = null;
            try {
                friends = (JsonObject) parser.parse(reader);
            } catch (ClassCastException e) {
                saveMutes();
            }
            JsonArray friendss = null;
            try {
                friendss = friends.getAsJsonArray("Mutes");
            } catch (Exception e) {
                System.err.println("Friend Array not found, skipping!");
            }

            if (friendss != null) {
                MuteManager.clearMuted();
                friendss.forEach(f -> parseFriend(f.getAsJsonObject()));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    private static void saveMutes() {
        try {
            JsonObject friendsObj = new JsonObject();
            friendsObj.add("Mutes", getMutedList());
            FileWriter writer = new FileWriter(Mutes);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(friendsObj, writer);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void load(File config) {
        // Read Config
        try {
            FileReader reader = new FileReader(config);
            JsonParser parser = new JsonParser();

            JsonArray array = null;
            try {
                array = (JsonArray) parser.parse(reader);
            } catch (ClassCastException e) {
                save(config);
            }

            JsonArray modules = null;
            try {
                JsonObject modulesObject = (JsonObject) array.get(0);
                modules = modulesObject.getAsJsonArray("Modules");
            } catch (Exception e) {
                System.err.println("Module Array not found, skipping!");
            }

            if (modules != null) {
                modules.forEach(m -> {
                    try {
                        parseModule(m.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        Client.moduleManager.load0();
        currentConfig = config;
        saveCurrentConfig();
        flag = true;
    }


    private static void saveFriends() {
        try {
            JsonObject friendsObj = new JsonObject();
            friendsObj.add("Friends", getFriendList());
            FileWriter writer = new FileWriter(Friends);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(friendsObj, writer);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void save(File config) {
        saveFriends();
        saveMutes();
        try {

            if (!config.exists()) {
                config.createNewFile();
            }

            JsonArray array = new JsonArray();

            JsonObject modulesObj = new JsonObject();
            modulesObj.add("Modules", getModuleArray());
            array.add(modulesObj);

//            JsonObject friendsObj = new JsonObject();
//            friendsObj.add("Friends", getFriendList());
//            array.add(friendsObj);


            FileWriter writer = new FileWriter(config);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(array, writer);
            writer.close();

        } catch (IOException e) {
            System.err.println("Cant write to config file!");
        }

    }


    private static void parseModule(JsonObject object) throws NullPointerException {

        Module module = ModuleManager.modules.stream()
                .filter(m -> object.getAsJsonObject(m.getName()) != null)
                .findFirst().orElse(null);

        if (module != null) {

            JsonObject moduleObject = object.getAsJsonObject(module.getName());

            try {
                for (Setting setting : ModuleManager.getSettings(module)) {
                    try {
                        if (setting.getValue() instanceof Float) {
                            setting.setValue(moduleObject.getAsJsonPrimitive(setting.getName()).getAsFloat());
                        } else if (setting.getValue() instanceof Double) {
                            setting.setValue(moduleObject.getAsJsonPrimitive(setting.getName()).getAsDouble());
                        } else if (setting.getValue() instanceof Integer) {
                            setting.setValue(moduleObject.getAsJsonPrimitive(setting.getName()).getAsInt());
                        } else if (setting.getValue() instanceof Boolean) {
                            setting.setValue(moduleObject.getAsJsonPrimitive(setting.getName()).getAsBoolean());
                            if (setting.getName().equals("Enabled")) {
                                if (moduleObject.getAsJsonPrimitive(setting.getName()).getAsBoolean()) {
                                    ForeverClient.EVENT_BUS.register(module);
                                } else {
                                    ForeverClient.EVENT_BUS.unregister(module);
                                }
                            }

                        } else if (setting.getValue() instanceof String) {
                            setting.setValue(moduleObject.getAsJsonPrimitive(setting.getName()).getAsString().replace("__", " "));
                        } else if (setting.getValue() instanceof Color) {
                            JsonArray array = moduleObject.getAsJsonArray(setting.getName());

                            setting.setColor(array.get(4).getAsFloat(), array.get(5).getAsFloat()
                                    , new Color(array.get(0).getAsInt(),
                                            array.get(1).getAsInt(),
                                            array.get(2).getAsInt(),
                                            array.get(3).getAsInt()));

                            EnumConverter converter = new EnumConverter(ColorPickerButton.Mode.class);
                            Enum<?> value = converter.doBackward(array.get(6).getAsJsonPrimitive());
                            setting.setMode(value == null ? ColorPickerButton.Mode.Normal : (ColorPickerButton.Mode) value);
                            setting.setSpeedColor(array.get(7).getAsInt());

                        } else if (setting.getValue() instanceof Enum) {
                            try {
                                EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                                Enum value = converter.doBackward(moduleObject.getAsJsonPrimitive(setting.getName()));
                                setting.setValue(value == null ? setting.getDefaultValue() : value);
                            } catch (Exception ignored) {
                            }
                        } else if (setting.getValue() instanceof Bind) {
                            setting.setValue(new Bind(moduleObject.getAsJsonPrimitive(setting.getName()).getAsInt()));
                        } else if (setting.getValue() instanceof Image) {
                            setting.setValue(new Image(moduleObject.getAsJsonPrimitive(setting.getName()).getAsString()));
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                throw new NullPointerException("Error loading module: " + module.getName());
            }
        } else {
            throw new NullPointerException("Couldn't find module");
        }
    }

    public static void setValueFromJson(Client feature, Setting setting, JsonElement element) {
        switch (setting.getType()) {
            case "Boolean":
                setting.setValue(element.getAsBoolean());
                break;
            case "Double":
                setting.setValue(element.getAsDouble());
                break;
            case "Float":
                setting.setValue(element.getAsFloat());
                break;
            case "Integer":
                setting.setValue(element.getAsInt());
                break;
            case "Color":
                String[] values = element.getAsString().split(",");
                setting.setColor(Float.parseFloat(values[4]), Float.parseFloat(values[5]), new Color(Integer.parseInt(values[0]), Integer.parseInt(values[1]),
                        Integer.parseInt(values[2]), Integer.parseInt(values[3])));
                setting.setRainbow(Boolean.parseBoolean(values[6]));
                setting.setSpeedColor(Integer.parseInt(values[7]));
                break;
            case "String":
                String str = element.getAsString();
                setting.setValue(str.replace("_", " "));
                break;
            case "Bind":
                setting.setValue(new Bind.BindConverter().doBackward(element));
                break;

            case "Enum":
                try {
                    EnumConverter converter = new EnumConverter(((Enum) setting.getValue()).getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue(value == null ? setting.getDefaultValue() : value);
                    break;
                } catch (Exception e) {
                    break;
                }

            default:
                ForeverClient.LOGGER.error("Unknown Setting type for: " + feature.getName() + " : " + setting.getName());
        }
    }

    private static void parseFriend(JsonObject friend) {
        if (friend.get("Name") != null && friend.get("UUID") != null) {
            Client.friendManager.addFriend(new FriendManager.Friend(friend.get("Name").getAsString(), UUID.fromString(friend.get("UUID").getAsString())));
        }
    }


    private static JsonArray getModuleArray() {
        JsonArray modulesArray = new JsonArray();
        for (Module m : ModuleManager.modules) {
            modulesArray.add(getModuleObject(m));
        }
        return modulesArray;
    }

    public static JsonObject getModuleObject(Module module) {
        JsonObject attribs = new JsonObject();
        for (Setting setting : ModuleManager.getSettings(module)) {
            if (setting == null) {
                System.out.println(module.getName());
                continue;
            }
            if (setting.isNumberSetting()) {
                attribs.addProperty(setting.getName(), (Number) setting.getValue());
            } else if (setting.getValue() instanceof Boolean) {
                attribs.addProperty(setting.getName(), (Boolean) setting.getValue());
            } else if (setting.getValue() instanceof Color) {
                float[] hsb = setting.getHSB();

                JsonArray array = new JsonArray();
                array.add(setting.getColor().getRed());
                array.add(setting.getColor().getGreen());
                array.add(setting.getColor().getBlue());
                array.add(setting.getColor().getAlpha());
                array.add(hsb[0]);
                array.add(hsb[1]);
                EnumConverter converter = new EnumConverter(ColorPickerButton.Mode.class);
                array.add(converter.doForward(setting.getColorMode()));
                array.add(setting.getSpeedColor());

                attribs.add(setting.getName(), array);
            } else if (setting.getValue() instanceof Bind) {
                attribs.addProperty(setting.getName(), ((Bind) setting.getValue()).getKey());
            } else if (setting.getValue() instanceof Enum) {
                EnumConverter converter = new EnumConverter(((Enum<?>) setting.getValue()).getClass());
                attribs.add(setting.getName(), converter.doForward((Enum<?>) setting.getValue()));
            } else if (setting.getValue() instanceof String) {
                String str = (String) setting.getValue();
                attribs.addProperty(setting.getName(), str.replace(" ", "__"));
            } else if (setting.getValue() instanceof Image) {
                if (((Image) setting.getValue()).getImageFile() == null) {
                    attribs.addProperty(setting.getName(), "NONE");
                } else {
                    attribs.addProperty(setting.getName(), ((Image) setting.getValue()).getImageFile().getAbsolutePath());
                }


            }
        }
        JsonObject moduleObject = new JsonObject();
        moduleObject.add(module.getName(), attribs);
        return moduleObject;
    }
    private static JsonArray getMutedList() {
        JsonArray mutedList = new JsonArray();

        for (String muted : MuteManager.getMuted()) {
            JsonObject mutedObj = new JsonObject();
            mutedObj.addProperty("Name", muted);
            mutedList.add(mutedObj);
        }
        return mutedList;
    }

    private static JsonArray getFriendList() {
        JsonArray friendsList = new JsonArray();

        for (FriendManager.Friend friend : Client.friendManager.getFriends()) {
            friendsList.add(getFriendObject(friend));
        }
        return friendsList;
    }

    private static JsonObject getFriendObject(FriendManager.Friend friend) {
        JsonObject friendObj = new JsonObject();
        friendObj.addProperty("Name", friend.getUsername());
        friendObj.addProperty("UUID", friend.getUuid().toString());
        return friendObj;
    }


    public static boolean delete(File file) {
        return file.delete();
    }

    public static List<File> getConfigList() {
        if (!CONFIGS.exists() || ForeverFolder.listFiles() == null) return null;

        if (CONFIGS.listFiles() != null) {
            return Arrays.stream(Objects.requireNonNull(CONFIGS.listFiles())).filter(f -> f.getName().endsWith(".json")).collect(Collectors.toList());
        }

        return null;
    }

}
