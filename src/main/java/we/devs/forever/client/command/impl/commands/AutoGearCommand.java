package we.devs.forever.client.command.impl.commands;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


public class AutoGearCommand extends Command {

    final static private String pathSave = "Forever/util/AutoReGear.json";
    private static final HashMap<String, String> errorMessage = new HashMap<String, String>() {
        {
            put("NoPar", "Not enough parameters");
            put("Exist", "This kit arleady exist");
            put("Saving", "Error saving the file");
            put("NoEx", "Kit not found");
        }
    };

    public AutoGearCommand() {
        super("kit",new SyntaxChunk("<add/save/del/set/list/help>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("a")) {
                    return "add";
                } else if (currentArg.toLowerCase().startsWith("d")) {
                    return "del";
                } else if (currentArg.toLowerCase().startsWith("l")) {
                    return "list";
                } else if (currentArg.toLowerCase().startsWith("sa")) {
                    return "save";
                } else if (currentArg.toLowerCase().startsWith("h")) {
                    return "help";
                } else if (currentArg.toLowerCase().startsWith("s")) {
                    return "set";
                }
                return currentArg;
            }
        }, new SyntaxChunk("<name>"));
    }

    private static void errorMessage(String e) {
        sendMessage("Error: " + errorMessage.get(e));
    }

    public static String getCurrentSet() {

        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            if (!completeJson.get("pointer").getAsString().equals("none"))
                return completeJson.get("pointer").getAsString();


        } catch (IOException e) {
            // Case not found, reset
        }
        errorMessage("NoEx");
        return "";
    }

    public static String getInventoryKit(String kit) {
        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            return completeJson.get(kit).getAsString();


        } catch (IOException e) {
            // Case not found, reset
        }
        errorMessage("NoEx");
        return "";
    }

    @Override
    public void execute(String[] commands) {
//        sendMessage("------------------------------");
//        Arrays.stream(commands).forEach(Command::sendMessage);
//        sendMessage("------------------------------");
        if (commands.length == 1) {
            sendMessage("You`ll find the config files in your gameProfile directory under client/config");
            return;
        }
        if (commands.length == 2) {
            if ("list".equals(commands[0])) {
                listMessage();
            }
        }
        if (commands.length >= 3) {
            switch (commands[0]) {
                case "set":
                    set(commands[1]);
                    break;
                case "save":
                case "add":
                case "create":
                    save(commands[1]);
                    break;
                case "del":
                    delete(commands[1]);
                    break;
                case "help":
                default:
                    sendMessage("AutoGear message is: gear set/save/del/list [name]");
                    break;
            }

        } else  errorMessage("NoPar");

    }

    private void listMessage() {
        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            int lenghtJson = completeJson.entrySet().size();
            for (int i = 0; i < lenghtJson; i++) {
                String item = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject().entrySet().toArray()[i].toString().split("=")[0];
                if (!item.equals("pointer"))
                    sendMessage("Kit avaible: " + item, false);
            }

        } catch (IOException e) {
            // Case not found, reset
            errorMessage("NoEx");
        }
    }

    private void delete(String name) {
        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            if (completeJson.get(name) != null && !name.equals("pointer")) {
                // Delete
                completeJson.remove(name);
                // Check if it's setter
                if (completeJson.get("pointer").getAsString().equals(name))
                    completeJson.addProperty("pointer", "none");
                // Save
                saveFile(completeJson, name, "deleted");
            } else errorMessage("NoEx");

        } catch (IOException e) {
            // Case not found, reset
            errorMessage("NoEx");
        }
    }

    private void set(String name) {
        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            if (completeJson.get(name) != null && !name.equals("pointer")) {
                // Change the value
                completeJson.addProperty("pointer", name);
                // Save
                saveFile(completeJson, name, "selected");
            } else errorMessage("NoEx");

        } catch (IOException e) {
            // Case not found, reset
            errorMessage("NoEx");
        }
    }

    private void save(String name) {
        JsonObject completeJson = new JsonObject();
        try {
            // Read json
            completeJson = new JsonParser().parse(new FileReader(pathSave)).getAsJsonObject();
            if (completeJson.get(name) != null && !name.equals("pointer")) {
                errorMessage("Exist");
                return;
            }
            // We can continue

        } catch (IOException e) {
            // Case not found, reset
            completeJson.addProperty("pointer", "none");
        }

        // String that is going to be our inventory
        StringBuilder jsonInventory = new StringBuilder();
        for (ItemStack item : mc.player.inventory.mainInventory) {
            // Add everything
            jsonInventory.append(item.getItem().getRegistryName().toString() + item.getMetadata()).append(" ");
        }
        // Add to the json
        completeJson.addProperty(name, jsonInventory.toString());
        // Save json
        saveFile(completeJson, name, "saved");
    }

    private void saveFile(JsonObject completeJson, String name, String operation) {
        // Save the json
        try {
            // Open
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathSave));
            // Write
            bw.write(completeJson.toString());
            // Save
            bw.close();
            // Chat message
            sendMessage("Kit " + name + " " + operation);
        } catch (IOException e) {
            errorMessage("Saving");
        }
    }
}