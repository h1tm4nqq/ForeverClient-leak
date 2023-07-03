package we.devs.forever.api.event.events.world;

import net.minecraft.block.Block;
import we.devs.forever.api.event.EventStage;

public class BlockInteractEvent extends EventStage {
    public Block block;

    public BlockInteractEvent(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
