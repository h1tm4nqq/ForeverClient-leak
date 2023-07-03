package we.devs.forever.api.event.events.player;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import we.devs.forever.api.event.EventStage;

import java.util.List;

public class BlockCollisionBoundingBoxEvent extends EventStage {


    private Block block;
    private IBlockState state;
    private World world;
    private BlockPos pos;
    private AxisAlignedBB entityBox;
    private List<AxisAlignedBB> collidingBoxes;
    private Entity entity;
    private boolean isActualState;

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setState(IBlockState state) {
        this.state = state;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setCollidingBoxes(List<AxisAlignedBB> collidingBoxes) {
        this.collidingBoxes = collidingBoxes;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setActualState(boolean actualState) {
        isActualState = actualState;
    }

    public BlockCollisionBoundingBoxEvent(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
        this.block = block;
        this.state = state;
        this.world = worldIn;
        this.pos = pos;
        this.entityBox = entityBox;
        this.collidingBoxes = collidingBoxes;
        this.entity = entityIn;
        this.isActualState = isActualState;
    }


    public Block getBlock() {
        return block;
    }

    public IBlockState getState() {
        return state;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AxisAlignedBB getEntityBox() {
        return entityBox;
    }

    public void setEntityBox(AxisAlignedBB entityBox) {
        this.entityBox = entityBox;
    }

    public List<AxisAlignedBB> getCollidingBoxes() {
        return collidingBoxes;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isActualState() {
        return isActualState;
    }
}
