package we.devs.forever.client.modules.impl.misc;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import we.devs.forever.api.event.events.world.BlockInteractEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;

public class NoOpen extends Module {
    public NoOpen() {
        super("NoOpen", "Cancel open packets", Category.PLAYER);
    }

     @EventListener
     public void onInteract(BlockInteractEvent event) {
        Block block = event.getBlock();
        if (block == Blocks.ANVIL
                || block == Blocks.CRAFTING_TABLE
                || block == Blocks.ACACIA_FENCE_GATE
                || block == Blocks.BIRCH_FENCE_GATE
                || block == Blocks.DARK_OAK_FENCE_GATE
                || block == Blocks.JUNGLE_FENCE_GATE
                || block == Blocks.SPRUCE_FENCE_GATE
                || block == Blocks.OAK_FENCE_GATE
                || block == Blocks.CHEST
                || block == Blocks.ENDER_CHEST
                || block == Blocks.ENCHANTING_TABLE
                || block == Block.getBlockById(63)  // стойка для брони
                || block == Blocks.FURNACE
                || block == Blocks.LIT_FURNACE
        ) {
            event.cancel();
        }
    }
}
