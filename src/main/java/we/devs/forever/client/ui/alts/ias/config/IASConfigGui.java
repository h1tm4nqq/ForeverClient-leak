package we.devs.forever.client.ui.alts.ias.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import we.devs.forever.client.ui.alts.ias.IAS;

public class IASConfigGui
        extends GuiConfig {
    public IASConfigGui(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(IAS.config.getCategory("general")).getChildElements(), "ias", false, false, GuiConfig.getAbridgedConfigPath(IAS.config.toString()));
    }
}

