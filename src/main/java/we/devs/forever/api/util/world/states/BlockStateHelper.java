package we.devs.forever.api.util.world.states;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import we.devs.forever.api.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@link IBlockAccess} that delegates all its methods
 * to <tt>mc.world</tt> except
 * {@link BlockStateHelper#getBlockState(BlockPos)}.
 * For the why read the documentation of of the
 * {@link BlockStateHelper#addBlockState(BlockPos, IBlockState)}
 * method.
 */
@SuppressWarnings({"NullableProblems", "unused"})
public class BlockStateHelper implements Util, IBlockStateHelper {
    protected final Map<BlockPos, IBlockState> states;
    protected Supplier<IBlockAccess> world;

    public BlockStateHelper() {
        this(new HashMap<>());
    }

    public BlockStateHelper(Supplier<IBlockAccess> world) {
        this(new HashMap<>(), world);
    }

    public BlockStateHelper(Map<BlockPos, IBlockState> stateMap) {
        this(stateMap, () -> mc.world);
    }

    public BlockStateHelper(Map<BlockPos, IBlockState> stateMap, Supplier<IBlockAccess> world) {
        this.states = stateMap;
        this.world = world;
    }

    /**
     * Returns an IBlockState set by
     * {@link BlockStateHelper#addBlockState(BlockPos, IBlockState)},
     * or if none was found the IBlockState from
     * {@link World#getBlockState(BlockPos)}.
     *
     * @param pos the position to get the BlockState for.
     * @return the BlockState at that Position.
     */
    @Override
    public IBlockState getBlockState(BlockPos pos) {
        IBlockState state = states.get(pos);
        if (state == null) {
            return world.get().getBlockState(pos);
        }

        return state;
    }
    @Override
    public void addBlockState(BlockPos pos, IBlockState state) {
        states.putIfAbsent(pos.toImmutable(), state);
    }

    @Override
    public void addBlockState(BlockPos pos, Block block) {
        states.putIfAbsent(pos.toImmutable(), block.defaultBlockState);
    }

    /**
     * Removes the custom IBlockState at the given pos.
     *
     * @param pos the pos to remove.
     */
    @Override
    public void delete(BlockPos pos) {
        states.remove(pos);
    }

    /**
     * Clears all BlockStates set by
     * {@link BlockStateHelper#addBlockState(BlockPos, IBlockState)}.
     */
    @Override
    public void clearAllStates() {
        states.clear();
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return world.get().getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return world.get().getCombinedLight(pos, lightValue);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return this.getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return world.get().getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return this.getBlockState(pos).getStrongPower(this, pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return world.get().getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (!mc.world.isValid(pos)) {
            return _default;
        }

        Chunk chunk = mc.world.getChunk(pos);
        //noinspection ConstantConditions
        if (chunk == null || chunk.isEmpty()) {
            return _default;
        }

        return this.getBlockState(pos).isSideSolid(this, pos, side);
    }

}
