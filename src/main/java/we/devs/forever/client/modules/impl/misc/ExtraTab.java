package we.devs.forever.client.modules.impl.misc;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class ExtraTab extends Module {
    private static ExtraTab INSTANCE = new ExtraTab();

    public Setting<Integer> size = new Setting<>("Size", 250, 1, 1000);

    public ExtraTab() {
        super("ExtraTab", "Extends Tab.", Category.MISC);
        this.setInstance();
    }

    public static String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
        String name;
        String string = name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
        if (friendManager.isFriend(name)) {
            return "\u00a7b" + name;
        }
        return name;
    }

    public static ExtraTab getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ExtraTab();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }
}