package we.devs.forever.client.command.impl.commands;

import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.impl.chunks.PlayerChunk;

import java.util.List;
import java.util.UUID;

public
class HistoryCommand extends Command {

    public HistoryCommand() {
        super("history", new PlayerChunk("<player>"));
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1 || commands.length == 0) { //idk lol
            sendMessage(TextUtil.RED + "Please specify a player.");
        }

        UUID uuid;
        try {
            uuid = PlayerUtil.getUUIDFromName(commands[0]);
        } catch (Exception e) {
            sendMessage("An error occured.");
            return;
        }

        List<String> names;
        try {
            names = PlayerUtil.getHistoryOfNames(uuid);
        } catch (Exception e) {
            sendMessage("An error occured.");
            return;
        }

        if (names != null) {
            sendMessage(commands[0] + "´s name history:");
            for (String name : names) {
                sendMessage(name);
            }
        } else {
            sendMessage("No names found.");
        }
    }
}
