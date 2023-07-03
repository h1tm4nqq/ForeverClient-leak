package we.devs.forever.api.manager.impl.client;

import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.impl.commands.*;
import we.devs.forever.client.modules.impl.chat.Notify;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public
class CommandManager extends AbstractManager {


    //public Setting<String> clientMessage = (new Setting<>("clientMessage", "<Phobos.eu>"));
    //public Setting<String> prefix = (new Setting<>("prefix", "."));
    private final ArrayList<Command> commands = new ArrayList<>();

    @Override
    public void onLoad() {
        commands.add(new AutoGearCommand());
        commands.add(new BindCommand());
        commands.add(new ModuleCommand());
        commands.add(new PrefixCommand());
        commands.add(new ConfigCommand());
        commands.add(new FriendCommand());
        commands.add(new HelpCommand());
        commands.add(new MuteCommand());
        commands.add(new ReloadCommand());
        commands.add(new SearchCommand());
        commands.add(new UnloadCommand());
        commands.add(new ReloadSoundCommand());
        commands.add(new BookCommand());
        commands.add(new CrashCommand());
        commands.add(new HistoryCommand());
        commands.add(new WayPointCommand());
    }

    @Override
    public void onUnload() {
        commands.clear();
    }

    private String prefix = "";

    public CommandManager() {
        super("Command");

    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        List<String> result = new LinkedList<>();
        for (int i = 0; i < input.length; i++) {
            if (i != indexToDelete) result.add(input[i]);
        }
        return result.toArray(input);
    }

    private static String strip(String str, String key) {
        if (str.startsWith(key) && str.endsWith(key))
            return str.substring(key.length(), str.length() - key.length());
        return str;
    }

    public void executeCommand(String command) {
        String[] parts = command.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = parts[0].substring(1);
        String[] args = removeElement(parts, 0);
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) continue;
            args[i] = strip(args[i], "\"");
        }
        for (Command c : commands) {
            if (c.getName().equalsIgnoreCase(name)) {
                c.execute(parts);
                return;
            }
        }
        Command.sendMessage("Unknown command. try 'commands' for a list of commands.");
    }

    public Command getCommandByName(String name) {
        for (Command command : commands) {
            if (command.getName().toLowerCase().equals(name)) {
                return command;
            }
        }
        return null;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public String getClientMessage() {
        if (Notify.rainbowPrefix.getValue()) {
            return TextUtil.RAINBOW_PLUS + "<ForeverClient>"+ TextUtil.RESET;
        }
        return TextUtil.coloredString("<", Notify.bracketColor.getValue())
                + TextUtil.coloredString("ForeverClient", Notify.commandColor.getValue())
                + TextUtil.coloredString(">", Notify.bracketColor.getValue());
    }

    public void setClientMessage(String clientMessage) {
        //this.clientMessage = TextUtil.coloredString("[", TextUtil.Color.WHITE) + TextUtil.coloredString(clientMessage, TextUtil.Color.DARK_PURPLE) + TextUtil.coloredString("]", TextUtil.Color.WHITE);
        //private String clientMessage = "ForeverClient >";
        String clientMessage1 = TextUtil.coloredString(clientMessage, TextUtil.Color.BLUE);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


}
