package we.devs.forever.client.command.api;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command extends Client {

    protected String name;
    protected SyntaxGroup group;

    public ArrayList<SyntaxChunk> getSyntaxChunks() {
        return group.getSyntaxChunks();
    }


    public Command(String name) {
        super(name);
        this.name = name;
        group = new SyntaxGroup();
        //this.syntaxChunks.add(new SyntaxChunk(name));
    }

    public Command(String name, SyntaxChunk... command) {
        super(name);
        this.name = name;
        Arrays.stream(command).forEach(syntaxChunk -> syntaxChunk.setCommand(this));
        group = new SyntaxGroup(command);
    }

    public static void sendMessage(String message, boolean notification) {
        sendSilentMessage(commandManager.getClientMessage() + " " + TextUtil.RESET + message);
        if (notification)
            notificationManager.addNotification(message, 3000);
    }


    public static void sendMessage(String message,Object... args) {
        sendSilentMessage(commandManager.getClientMessage() + " " + TextUtil.RESET + String.format(message,args));
    }
    public static void sendMessagepref(String message, String prefix) {
        if (nullCheck()) return;
        int moduleNumber = 0;
        for (char character : prefix.toCharArray()) {
            moduleNumber += character;
            moduleNumber *= 10;
        }
        TextComponentString textComponentString = new TextComponentString(message);
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(textComponentString, moduleNumber);
    }

    public static void sendSilentMessage(String message) {
        if (nullCheck()) return;
        mc.player.sendMessage(new ChatMessage(message));
    }

    public static String getCommandPrefix() {
        return commandManager.getPrefix();
    }

    public abstract void execute(String[] commands);// {}

    public String getName() {
        return this.name;
    }

    public String complete(String str) {
        if (name.toLowerCase().startsWith(str.toLowerCase())) {
            return name;
        }
//        for (String alias : commands) {
//            if (alias.toLowerCase().startsWith(str)) {
//                return alias;
//            }
//        }
        return null;
    }


    public static
    class ChatMessage extends TextComponentBase {

        private final String text;

        public ChatMessage(String text) {
            Pattern pattern = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher matcher = pattern.matcher(text);
            StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                String replacement = "\u00A7" + matcher.group().substring(1);
                matcher.appendReplacement(stringBuffer, replacement);
            }
            matcher.appendTail(stringBuffer);
            this.text = stringBuffer.toString();
        }

        public String getUnformattedComponentText() {
            return text;
        }

        @Override
        public ITextComponent createCopy() {
            return new ChatMessage(text);
        }
    }
}
