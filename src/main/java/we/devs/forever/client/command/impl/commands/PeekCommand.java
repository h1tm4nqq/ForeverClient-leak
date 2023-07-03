package we.devs.forever.client.command.impl.commands;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.impl.chunks.PlayerChunk;
import we.devs.forever.client.modules.impl.render.ToolTips;

import java.util.Map;

public class PeekCommand
        extends Command {
    public PeekCommand() {
        super("peek", new PlayerChunk("<player>"));
    }

    @Override
    public void execute(String[] commands) {
        if (commands.length == 1) {
            ItemStack stack = PeekCommand.mc.player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemShulkerBox) {
                ToolTips.displayInv(stack, null);
            } else {
                Command.sendMessage("\u00a7cYou need to hold a Shulker in your mainhand.");
                return;
            }
        }
        if (commands.length > 1) {
            if (ToolTips.getInstance().isEnabled() && ToolTips.getInstance().shulkerSpy.getValue()) {
                for (Map.Entry<EntityPlayer, ItemStack> entry : ToolTips.getInstance().spiedPlayers.entrySet()) {
                    if (!entry.getKey().getName().equalsIgnoreCase(commands[0])) continue;
                    ItemStack stack = entry.getValue();
                    ToolTips.displayInv(stack, entry.getKey().getName());
                    break;
                }
            } else {
                Command.sendMessage("\u00a7cYou need to turn on Tooltips - ShulkerSpy");
            }
        }
    }
}
