package we.devs.forever.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;

public
interface Util {

    Minecraft mc = Minecraft.getMinecraft();
    FontRenderer fr = Util.mc.fontRenderer;

    static EntityPlayerSP getPlayer() {
        return mc.player;
    }
}
