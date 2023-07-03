package we.devs.forever.client.ui.alts.ias;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import we.devs.forever.client.ui.alts.ias.config.ConfigValues;

//@Mod(modid="ias", name="Forever Client Account Switcher", clientSideOnly=true, guiFactory="we.devs.forever.client.gui.alts.ias.config.IASGuiFactory", updateJSON="https://thefireplace.bitnamiapp.com/jsons/ias.json", acceptedMinecraftVersions="[1.11,)")
public class IAS {
    public static Configuration config;
    public static Property CASESENSITIVE_PROPERTY;
    public static Property ENABLERELOG_PROPERTY;

    public static void syncConfig() {
        ConfigValues.CASESENSITIVE = CASESENSITIVE_PROPERTY.getBoolean();
        ConfigValues.ENABLERELOG = ENABLERELOG_PROPERTY.getBoolean();
        if (config.hasChanged()) {
            config.save();
        }
    }


}

