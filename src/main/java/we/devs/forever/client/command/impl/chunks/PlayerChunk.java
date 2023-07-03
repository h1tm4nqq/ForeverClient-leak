package we.devs.forever.client.command.impl.chunks;

import net.minecraft.client.network.NetworkPlayerInfo;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.client.command.impl.commands.FriendCommand;

import java.util.Random;

public class PlayerChunk extends SyntaxChunk {
    public PlayerChunk(String name) {
        super(name);
    }

    private static final Random random = new Random();

    @Override
    public String predict(String currentArg) {
        if (getCommand() instanceof FriendCommand) {
            SyntaxChunk chunk = getGroup().findChunk("<add/del/list/isFriend>");
            if (chunk.getValue().equals("add")) {
                for (NetworkPlayerInfo player : mc.getConnection().getPlayerInfoMap()) {
                    if (friendManager.isFriend(player.getGameProfile().getName())) continue;
                    if (player.getGameProfile().getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                        return player.getGameProfile().getName();
                    }
                }
            }
            if (chunk.getValue().equals("del")) {
                for (NetworkPlayerInfo player : mc.getConnection().getPlayerInfoMap()) {
                    if (!friendManager.isFriend(player.getGameProfile().getName())) continue;
                    if (player.getGameProfile().getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                        return player.getGameProfile().getName();
                    }
                }
            }
            if (chunk.getValue().equals("list")) {
                return "";
            }
            if (chunk.getValue().equals("isFriend")) {
                for (NetworkPlayerInfo player : mc.getConnection().getPlayerInfoMap()) {
                    if (player.getGameProfile().getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                        return player.getGameProfile().getName();
                    }
                }
            }
        } else {
            for (NetworkPlayerInfo player : mc.getConnection().getPlayerInfoMap()) {
                if (player.getGameProfile().getName().toLowerCase().startsWith(currentArg.toLowerCase())) {
                    return player.getGameProfile().getName();
                }
            }
        }
        return currentArg;
    }

    @Override
    public String getName() {
        if (getCommand() instanceof FriendCommand) {
            SyntaxChunk chunk = getGroup().findChunk("<add/del/list/isFriend>");
            if (chunk == null || chunk.getValue() == null) return super.getName();
            if (chunk.getValue().equals("list")) {
                return "";
            }
        }
        return super.getName();
    }
}
