package we.devs.forever.client.command.impl.commands;


import net.minecraft.block.Block;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.modules.impl.render.search.Search;

public class SearchCommand extends Command {
    public SearchCommand() {
        super("search", new SyntaxChunk("<add/del/list>"), new SyntaxChunk("[block]"));
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                if (Search.customBlocks.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                   Search.customBlocks.forEach(str -> sb.append(str).append("\n"));
                    Command.sendMessage(sb.toString());
                } else {
                    Command.sendMessage("You dont have any blocks added :(");
                }
            } else {
                Command.sendMessage("err 1");
            }
            // return;
        }


        if (args[0].equals("add")) {
            if(Search.customBlocks.add(Block.getBlockFromName(args[1]))) {
                Command.sendMessage("Added Block &b" + args[1]);
                if (Search.INSTANCE.softReload.getValue()) {
                    Search.doSoftReload();
                }
            } else {
                Command.sendMessage("Couldn't find block &b" + args[1]);
            }
        } else if (args[0].equals("del")) {
            if(Search.customBlocks.remove(Block.getBlockFromName(args[1]))) {
                Command.sendMessage("Removed Block &b" + args[1]);
            } else {
                Command.sendMessage("Couldn't find block &b" + args[1]);
            }
        } else {
            Command.sendMessage("Couldn't find block &b" + args[1]);
        }
    }

}

