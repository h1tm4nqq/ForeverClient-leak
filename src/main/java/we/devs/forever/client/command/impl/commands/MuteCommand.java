package we.devs.forever.client.command.impl.commands;


import we.devs.forever.api.manager.impl.client.MuteManager;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.command.impl.chunks.PlayerChunk;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("mute", new SyntaxChunk("<add/del/clear/list>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("a")) {
                    return "add";
                } else if (currentArg.toLowerCase().startsWith("d")) {
                    return "del";
                } else if (currentArg.toLowerCase().startsWith("c")) {
                    return ("clear");
                } else if (currentArg.toLowerCase().startsWith("l")) {
                    return "list";
                }
                return currentArg;
            }
        }, new PlayerChunk("[name]"));
    }

    @Override
    public void execute(String[] args) {

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!MuteManager.getMuted().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nMute List:\n");
                    MuteManager.getMuted().stream().forEach(member -> sb.append(member + "\n"));
                     sendMessage(sb.toString());
                } else {
                     sendMessage("You don't have anyone muted");
                }
            } else if (args[1].equalsIgnoreCase("clear")) {
                if(!MuteManager.getMuted().isEmpty()) {
                    MuteManager.clearMuted();
                     sendMessage("Cleared your muted list!");
                } else {
                    sendMessage("Your muted list is already clear!");
                }
            } else {
                sendMessage("Invalid Argument. Check syntax");
            }
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            for (String member : MuteManager.getMuted()) {
                if (member.equalsIgnoreCase(args[2])) {
                    sendMessage("Player §b" + args[2] + "§r is already muted!");
                    return;
                }
            }
            MuteManager.addMuted(args[2]);
             sendMessage("Muted §b" + args[2]);
        } else if (args[1].equalsIgnoreCase("del")) {
            for (String member : MuteManager.getMuted()) {
                if (member.equalsIgnoreCase(args[2])) {
                    MuteManager.removeMuted(member);
                    sendMessage("Unmuted §b" + args[2]);
                    return;
                }
            }
            sendMessage("Player §b" + args[2] + "§r is not muted!");
        }

    }

}
