package we.devs.forever.client.modules.impl.misc;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class AutoReconnect extends Module {
    private static ServerData serverData;
    private static AutoReconnect INSTANCE;

    static {
        AutoReconnect.INSTANCE = new AutoReconnect();
    }

    private final Setting<Integer> delay;

    public AutoReconnect() {
        super("AutoReconnect", "Reconnects you if you disconnect.", Category.MISC);
        this.delay = (Setting<Integer>) (new Setting<>("Delay", 5));
        this.setInstance();
    }


    private void setInstance() {
        AutoReconnect.INSTANCE = this;
    }

    @SubscribeEvent
    public void sendPacket(final GuiOpenEvent event) {
        if (event.getGui() instanceof GuiDisconnected) {
            this.updateLastConnectedServer();
            if (AutoLog.INSTANCE.isDisabled()) {
                final GuiDisconnected disconnected = (GuiDisconnected) event.getGui();
                event.setGui(new GuiDisconnectedHook(disconnected));
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        this.updateLastConnectedServer();
    }

    public void updateLastConnectedServer() {
        final ServerData data = AutoReconnect.mc.getCurrentServerData();
        if (data != null) {
            AutoReconnect.serverData = data;
        }
    }

    private class GuiDisconnectedHook extends GuiDisconnected {
        private final TimerUtil timerUtil;

        public GuiDisconnectedHook(final GuiDisconnected disconnected) {
            super(null, null, null);//(disconnected.parentScreen, disconnected.reason, disconnected.message);
            (this.timerUtil = new TimerUtil()).reset();
        }

        public void updateScreen() {
            if (this.timerUtil.passedS(AutoReconnect.this.delay.getValue())) {
                //this.mc.displayGuiScreen((GuiScreen)new GuiConnecting(this.parentScreen, this.mc, (AutoReconnect.serverData == null) ? this.mc.currentServerData : AutoReconnect.serverData));
            }
        }

        public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            final String s = "Reconnecting in " + MathUtil.round((AutoReconnect.this.delay.getValue() * 1000 - this.timerUtil.getPassedTimeMs()) / 1000.0, 1);
            AutoReconnect.this.renderer.drawString(s, (float) (this.width / 2 - AutoReconnect.this.renderer.getStringWidth(s) / 2), (float) (this.height - 16), 16777215, true);
        }
    }
}
