package we.devs.forever.api.event.events.player;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.EventStage;

public class BlockRenderEvent extends EventStage {
    private Block block;
    private BlockPos pos;

    public BlockRenderEvent(Block block, BlockPos pos) {
        this.block = block;
        this.pos = pos;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getPos() {
        return pos;
    }
}
