package we.devs.forever.client.modules.impl.chat;

import net.minecraft.network.play.client.CPacketChatMessage;
import we.devs.forever.api.util.client.FileUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public
class Spammer extends Module {

    private static final String fileName = "Forever/util/Spammer.txt";
    private static final String defaultMessage =  " The Forever on top ";
    private static final List<String> spamMessages = new ArrayList();
    private static final Random rnd = new Random();
    private final TimerUtil timerUtil = new TimerUtil();
    private final List<String> sendPlayers = new ArrayList<>();
    public  Setting<Integer> delay = (new Setting<>("Delay", 10, 1, 20, "Spamms messages with delay"));
    public Setting<Boolean> random = (new Setting<>("Random", false,"Randomly spamms messages"));
    public Setting<Boolean> loadFile = (new Setting<>("LoadFile", false, "Loads messages from your file"));

    public Spammer() {
        super("Spammer", "Automatically spams in chat", Category.CHAT);
    }

    @Override
    public void onLoad() {
        readSpamFile();
        this.disable();
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }
        readSpamFile();
    }

    @Override
    public void onLogin() {
        this.disable();
    }

    @Override
    public void onLogout() {
        this.disable();
    }

    @Override
    public void onDisable() {
        spamMessages.clear();
        timerUtil.reset();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }

        if (loadFile.getValue()) {
            readSpamFile();
            loadFile.setValue(false);
        }

        if (!timerUtil.passedS(delay.getValue())) {
            return;
        }

        if (spamMessages.size() > 0) {
            String messageOut;
            if (random.getValue()) {
                int index = rnd.nextInt(spamMessages.size());
                messageOut = spamMessages.get(index);
                spamMessages.remove(index);
            } else {
                messageOut = spamMessages.get(0);
                spamMessages.remove(0);
            }
            spamMessages.add(messageOut);

            mc.player.connection.sendPacket(new CPacketChatMessage(messageOut.replaceAll(TextUtil.SECTIONSIGN, "")));
        }
        timerUtil.reset();
    }

    private void readSpamFile() {
        List<String> fileInput = FileUtil.readTextFileAllLines(fileName);
        Iterator<String> i = fileInput.iterator();
        spamMessages.clear();
        while (i.hasNext()) {
            String s = i.next();
            if (!s.replaceAll("\\s", "").isEmpty()) {
                spamMessages.add(s);
            }
        }
        if (spamMessages.size() == 0) {
            spamMessages.add(defaultMessage);
        }
    }
}
