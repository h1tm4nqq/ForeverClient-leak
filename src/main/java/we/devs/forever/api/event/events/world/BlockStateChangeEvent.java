package we.devs.forever.api.event.events.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.mixin.ducks.IChunk;

public class BlockStateChangeEvent {
    private final BlockPos pos;
    private final IBlockState state;
    private final IChunk chunk;

    public BlockStateChangeEvent(BlockPos pos, IBlockState state, IChunk chunk) {
        this.pos = pos;
        this.state = state;
        this.chunk = chunk;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public IBlockState getState() {
        return this.state;
    }

    public IChunk getChunk() {
        return this.chunk;
    }
}

