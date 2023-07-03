package we.devs.forever.client.modules.impl.client;

import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.main.Discord;

public class RPC
        extends Module {
    public static RPC INSTANCE;

    public final Setting<Mode> mode = (new Setting<>("Mode:", Mode.Forever));
    public Setting<Boolean> showIP = (new Setting<>("ShowIP", true, "Shows the server IP in your discord presence."));
    public Setting<Text> text = (new Setting<>("LargeImageText:", Text.None, "Sets the state of the DiscordRPC."));
    public Setting<String> customText = (new Setting<>("CustomLargeImageText", "RPC by hitmanqq", V -> text.getValue() == Text.Custom));
    public Setting<Text> text1 = (new Setting<>("DetailsText:", Text.None, "Sets the state of the DiscordRPC."));
    public Setting<String> customText1 = (new Setting<>("CustomText", "RPC by hitmanqq", V -> text1.getValue() == Text.Custom));


    public RPC() {
        super("RPC", "Discord rich presence", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {

        Discord.start();
    }

    @Override
    public void onDisable() {
        Discord.stop();
    }


    public enum Mode {
        Forever,
        Anime,
        NeTrO,
        Cat
    }

    public enum Text {
        Random,
        TheForever,
        ForeverClient,
        Custom,
        None
    }
}

