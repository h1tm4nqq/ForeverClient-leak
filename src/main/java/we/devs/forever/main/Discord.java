package we.devs.forever.main;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import we.devs.forever.client.modules.impl.client.RPC;
import we.devs.forever.client.ui.customScreen.GuiCustomMainScreen;

import java.util.Random;

import static we.devs.forever.api.util.Util.mc;


public class Discord {

    private static final DiscordRPC rpc;
    public static DiscordRichPresence presence;
    static String[] random = {
            "Fuck You",
            "I love gel hair removal",
            "Debug: run SystemUtil.class.....",
            "Ezzzzzzzzzzzzzzzz rat",
            "Debug: run DiscordSteal.class....",
            "Debug: crack your mom",
            "Debug: your mom has been decompile",
            "The Forever on top!",
            "I want a new monitor",
            "Debug: run Hwid.class.....",
            "dflpkgjifdpiogijfpiglfgpfpgfgf",
            "\\u0020\\ud835\\ude75",
            "ezzzzzzzzzzzzzzzzzzzzz pop",
            "Cope nn",
            "drawSexyBoxPhobosIsRetardedFuckYouESP(lel);"

    };
    private static Thread thread;
    private static int index;

    static {
        index = 1;
        rpc = DiscordRPC.INSTANCE;
        presence = new DiscordRichPresence();
    }

    public static void start() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        rpc.Discord_Initialize("1091799065274097865", handlers, true, "");
        Discord.presence.startTimestamp = System.currentTimeMillis() / 1000L;
        rpc.Discord_UpdatePresence(presence);
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                rpc.Discord_RunCallbacks();
                LargeImageText();
                DetailsText();
                Discord.presence.details = getDetals();
                switch (RPC.INSTANCE.mode.getValue()) {
                    case Anime:
                        if (index == 17) {
                            index = 1;
                        }
                        Discord.presence.largeImageKey = "foreverclient" + index;
                        ++index;
                        break;
                    case Forever:
                        Discord.presence.largeImageKey = "forever";

                        break;
                    case NeTrO:
                        Discord.presence.largeImageKey = "netro";
                        break;
                    case Cat:
                        Discord.presence.largeImageKey = "cat";
                        break;
                }

                rpc.Discord_UpdatePresence(presence);
                try {
                    Thread.sleep(4000L);
                } catch (InterruptedException ignored) {
                }
            }
        }, "RPC-Callback-Handler");
        thread.start();
    }

    private static String getDetals() {
        final String user = " | " + mc.getSession().getUsername();
        if (mc.currentScreen instanceof GuiCustomMainScreen || mc.currentScreen instanceof GuiMainMenu) {
            return "In the mainMenu" + user;
        }
        if (mc.currentScreen instanceof GuiMultiplayer) {
            return "Choose server" + user;
        }
        if (Minecraft.getMinecraft().currentServerData != null) {
            return RPC.INSTANCE.showIP.getValue() ? "on " + mc.currentServerData.serverIP + user : "Playing online" + user;
        }
        return "playing offline" + user;
    }

    public static void stop() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }

    private static void LargeImageText() {
        switch (RPC.INSTANCE.text.getValue()) {
            case None:
                Discord.presence.largeImageText = "";
                break;
            case Custom:
                Discord.presence.largeImageText = RPC.INSTANCE.customText.getValue();
                break;
            case TheForever:
                Discord.presence.largeImageText = "\u0020\ud835\ude83\ud835\ude91\ud835\ude8e\u0020\ud835\ude75\ud835\ude98\ud835\ude9b\ud835\ude8e\ud835\ude9f\ud835\ude8e\ud835\ude9b\u0020\ud835\ude7e\ud835\ude97\u0020\ud835\ude83\ud835\ude98\ud835\ude99\u0020";
                break;
            case Random:
                Discord.presence.largeImageText = random[new Random().nextInt(random.length)];
                break;
            case ForeverClient:
                Discord.presence.largeImageText = "\u0020\ud835\ude75\ud835\ude98\ud835\ude9b\ud835\ude8e\ud835\ude9f\ud835\ude8e\ud835\ude9b\u0020\ud835\ude72\ud835\ude95\ud835\ude92\ud835\ude8e\ud835\ude97\ud835\ude9d\u0020\u0020" + " v." + ForeverClient.MODVER;
        }
    }

    private static void DetailsText() {
        switch (RPC.INSTANCE.text1.getValue()) {
            case None:
                Discord.presence.state = "";
                break;
            case Custom:
                Discord.presence.state = RPC.INSTANCE.customText1.getValue();
                break;
            case TheForever:
                Discord.presence.state = "\u0020\ud835\ude83\ud835\ude91\ud835\ude8e\u0020\ud835\ude75\ud835\ude98\ud835\ude9b\ud835\ude8e\ud835\ude9f\ud835\ude8e\ud835\ude9b\u0020\ud835\ude7e\ud835\ude97\u0020\ud835\ude83\ud835\ude98\ud835\ude99\u0020";
                break;
            case Random:
                Discord.presence.state = random[new Random().nextInt(random.length)];
                break;
            case ForeverClient:
                Discord.presence.state = "\u0020\ud835\ude75\ud835\ude98\ud835\ude9b\ud835\ude8e\ud835\ude9f\ud835\ude8e\ud835\ude9b\u0020\ud835\ude72\ud835\ude95\ud835\ude92\ud835\ude8e\ud835\ude97\ud835\ude9d\u0020\u0020" + " v." + ForeverClient.MODVER;
        }
    }

}

