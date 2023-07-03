package we.devs.forever.client.command.impl.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.command.impl.chunks.PlayerChunk;

public
class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", new SyntaxChunk("<add/del/list/isFriend>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("a")) {
                    return "add";
                } else if (currentArg.toLowerCase().startsWith("d")) {
                    return "del";
                } else if (currentArg.toLowerCase().startsWith("l")) {
                    return "list";
                } else if (currentArg.toLowerCase().startsWith("i")) {
                    return "isFriend";
                }
                return currentArg;
            }
        }, new PlayerChunk("<player>"));
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {

            return;
        }

//        if (commands.length == 2) {
//            if (commands[0].equals("reset")) {
//                friendManager.onLoad();
//                sendMessage("Friends got reset.");
//            } else {
//                sendMessage(commands[0] + (friendManager.isFriend(commands[0]) ? " is friended." : " isnt friended."));
//            }
//            return;
//        }

        if (commands.length >= 2) {
            switch (commands[0]) {
                case "add":
                    friendManager.addFriend(commands[1]);
                    sendMessage(TextUtil.AQUA + commands[1] + " has been friended");
                    break;
                case "del":
                    friendManager.removeFriend(commands[1]);
                    sendMessage(TextUtil.RED + commands[1] + " has been unfriended");
                    break;
                case "list":
                    if (friendManager.getFriends().isEmpty()) {
                        sendMessage("You currently don't have any friends added.");
                    } else {
                        StringBuilder f = new StringBuilder("Friends: ");
                        friendManager.getFriends()
                                .stream()
                                .map(friend -> {
                                    try {
                                        return friend.getUsername();
                                    } catch (Throwable ignored) {
                                        return "null";
                                    }
                                }).filter(s -> !s.equals("null"))
                                .sorted(String::compareTo)
                                .forEach(s -> f.append(s).append(","));
                        f.append(" ").append(ChatFormatting.GREEN).append(friendManager.getFriends().size());
                        sendMessage(f.toString());
                    }
                    break;
                case "isFriend":
                    sendMessage(commands[1] + (friendManager.isFriend(commands[1]) ? TextUtil.GREEN + " is friend." : TextUtil.RED + " isn't friend."));
                break;
                default:
                    sendMessage(TextUtil.RED + "Bad Command, try: friend <add/del/list/isFriend> <name>.");
            }
        }
    }
}
