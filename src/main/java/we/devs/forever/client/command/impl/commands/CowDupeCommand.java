package we.devs.forever.client.command.impl.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CowDupeCommand {

    @SubscribeEvent
    public static void onPlayerChat(final ClientChatEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (e.getMessage().equals("@cowdupe")) {
            e.setCanceled(true);
            if (mc.player.inventory.getCurrentItem().getItem().equals(Items.SHEARS)) {
                for (int i = 0; i < 150; ++i) {
                    if (mc.pointedEntity != null) {
                        mc.getConnection().sendPacket(new CPacketUseEntity(mc.pointedEntity, EnumHand.MAIN_HAND));
                    }
                }
                mc.ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentString("Finished shearing targeted entity."));
            } else {
                mc.ingameGUI.addChatMessage(ChatType.CHAT, new TextComponentString("You need to hold shears to do the glitch."));
            }
        }
    }
}