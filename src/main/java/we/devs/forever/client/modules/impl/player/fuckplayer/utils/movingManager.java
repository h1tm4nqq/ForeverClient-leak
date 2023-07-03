package we.devs.forever.client.modules.impl.player.fuckplayer.utils;

import we.devs.forever.client.modules.impl.player.fuckplayer.FuckPlayer;
import we.devs.forever.client.modules.impl.player.fuckplayer.enums.MoveMode;

import java.util.ArrayList;

public class movingManager extends FuckPlayer {

    public final ArrayList<movingPlayer> players;

    public movingManager() {
        this.players = new ArrayList<movingPlayer>();
    }

    public void addPlayer(final int id, final MoveMode type, final double speed, final int direction, final double range, final boolean follow) {
        this.players.add(new movingPlayer(id, type, speed, direction, range, follow));
    }

    public void update() {
        this.players.forEach(movingPlayer::move);
    }

    public void remove() {
        this.players.clear();
    }

}
