package we.devs.forever.client.modules.impl.misc;

import net.minecraft.client.gui.GuiGameOver;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class AutoRespawn extends Module {
    private final TimerUtil timer;
    public Setting<Boolean> autoKit = (new Setting<>("Auto Kit", false));
    public Setting<String> kit = (new Setting<>("KitName", "KitName", v -> autoKit.getValue()));
    public AutoRespawn() {
        super("AutoRespawn", "Auto respawn", Category.MISC);
        this.timer = new TimerUtil();
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        if (timer.passedMs(2100)) {
            timer.reset();
        }
        if (mc.currentScreen instanceof GuiGameOver) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
        if (mc.currentScreen instanceof GuiGameOver && this.timer.getPassedTimeMs() > 200) {
            if (autoKit.getValue()) {
                mc.player.sendChatMessage("/kit " + kit.getValue());
            }
            timer.reset();
        }
    }
}