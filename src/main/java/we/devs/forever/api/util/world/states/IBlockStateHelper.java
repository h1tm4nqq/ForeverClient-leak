package we.devs.forever.api.util.world.states;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockStateHelper extends IBlockAccess
{
    default void addAir(BlockPos pos)
    {
        this.addBlockState(pos, Blocks.AIR.getDefaultState());
    }

    void addBlockState(BlockPos pos, IBlockState state);
    void addBlockState(BlockPos pos, Block block);

    void delete(BlockPos pos);

    void clearAllStates();

}
