package we.devs.forever.client.modules.impl.chat;

import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiSpam extends Module {
    public Setting<Boolean> rce = (new Setting<>("Log4j", true, "Log4j"));
    public Setting<Boolean> popLag = (new Setting<>("PopLag", true, "PopLag"));
    public Setting<Boolean> url = (new Setting<>("URL", false, "url"));
    public Setting<Boolean> discord = (new Setting<>("Discord", false, "Discord channels"));
    public Setting<Boolean> youtube = (new Setting<>("YouTube", false, "Youtube channels"));
    public Setting<Boolean> announcer = (new Setting<>("Announcer", true, "Announcements"));
    public Setting<Boolean> shops = (new Setting<>("Shops", false, "Shops"));
    public Setting<Boolean> toxic = (new Setting<>("Toxic", false, "Toxic"));
    public AntiSpam() {
        super("AntiSpam", "Blocked spams messages", Category.CHAT);
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (popLag.getValue()) {
            if (event.getPacket() instanceof SPacketChat) {
                TextComponentString component = (TextComponentString) ((SPacketChat) event.getPacket()).getChatComponent();
                String text = component.getText();
                Pattern pattern = Pattern.compile("[\\x00-\\x7F]", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    String[] rceA = new String[]{
            "jndi:ldap",
            "$:-",
            "$<"
    };
    String[] urlA = new String[] {
            "http",
            "www.",
            ".cc",
            ".ru",
            ".org",
            ".com",
            ".eu",
            ".xyz",
            ".net",
            ".dev",
            ".me",
            ",cc",
            "www,",
            ",ru",
            ",org",
            ",com",
            ",eu",
            ",xyz",
            ",net",
            ",dev",
            ",us",
            ",me"
    };
    String[] toxicA = new String[]{
            "ez",
            "EZ",
            "izi",
            "IZI",
            "pop",
            "POP",
            "cry",
            "CRY"
    };
    String[] shopsA = new String[]{
            "join",
            "JOIN",
            "top",
            "TOP",
            "buy",
            "BUY",
            "shop",
            "SHOP"
    };
    String[] youtubeA = new String[]{
            "youtu.be",
            "youtube.com"
    };
    String[] announcerA = new String[]{
            "Looking for new anarchy servers?",
            "I just walked",
            "I just flew",
            "I just placed",
            "I just ate",
            "I just healed",
            "I just took",
            "I just spotted",
            "I walked",
            "I flew",
            "I placed",
            "I ate",
            "I healed",
            "I took",
            "I gained",
            "I mined",
            "I lost",
            "I moved"
    };
    @EventListener(priority = ListenerPriority.HIGHEST)
    public void onPacketRecieve(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;

        if (!(event.getPacket() instanceof SPacketChat)) {
            return;
        }
        SPacketChat chatMessage = (SPacketChat) event.getPacket();
        if (detectSpam(chatMessage.getChatComponent().getUnformattedText())) {
            event.cancel();
        }
    }

    private boolean detectSpam(String message) {
        if (rce.getValue()) {
            for (String discordSpam : rceA) {
                if (message.contains(discordSpam)) {
                    return true;
                }
            }
        }

        if (announcer.getValue()) {
            for (String announcerSpam : announcerA) {
                if (message.contains(announcerSpam)) {
                    return true;
                }
            }
        }

        if (url.getValue()) {
            for (String domainSpam : urlA) {
                if (message.contains(domainSpam)) {
                    return true;
                }
            }
        }
        if (toxic.getValue()) {
            for (String domainSpam : toxicA) {
                if (message.contains(domainSpam)) {
                    return true;
                }
            }
        }
        if (shops.getValue()) {
            for (String domainSpam : shopsA) {
                if (message.contains(domainSpam)) {
                    return true;
                }
            }
        }
        if (youtube.getValue()) {
            for (String domainSpam : youtubeA) {
                if (message.contains(domainSpam)) {
                    return true;
                }
            }
        }
        if (discord.getValue()) {
            return message.contains("discord.gg");
        }

        return false;
    }


}