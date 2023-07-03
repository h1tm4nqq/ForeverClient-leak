package we.devs.forever.client.ui.alts.ias.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import we.devs.forever.client.ui.alts.ias.IAS;
import we.devs.forever.client.ui.alts.ias.gui.GuiAccountSelector;
import we.devs.forever.client.ui.alts.ias.gui.GuiButtonWithImage;
import we.devs.forever.client.ui.alts.tools.Config;

public class ClientEvents {
    @SubscribeEvent
    public void guiEvent(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiMainMenu) {
            event.getButtonList().add(new GuiButtonWithImage(20, gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12, 20, 20, ""));
        }
    }

    @SubscribeEvent
    public void onClick(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.getGui() instanceof GuiMainMenu && event.getButton().id == 20) {
            if (Config.getInstance() == null) {
                Config.load();
            }
            Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelector());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent t) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiMainMenu) {
            screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format("ias.loggedinas", new Object[0]) + Minecraft.getMinecraft().getSession().getUsername() + ".", screen.width / 2, screen.height / 4 + 48 + 72 + 12 + 22, -3372920);
        } else if (screen instanceof GuiMultiplayer && Minecraft.getMinecraft().getSession().getToken().equals("0")) {
            screen.drawCenteredString(Minecraft.getMinecraft().fontRenderer, I18n.format("ias.offlinemode", new Object[0]), screen.width / 2, 10, 0xFF6464);
        }
    }

    @SubscribeEvent
    public void configChanged(ConfigChangedEvent event) {
        if (event.getModID().equals("ias")) {
            IAS.syncConfig();
        }
    }
}

