package we.devs.forever.api.event.events.world;


import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.EventStage;


public
class BlockEvent extends EventStage {

    public BlockPos pos;
    public EnumFacing facing;

    public BlockEvent(int stage, BlockPos pos, EnumFacing facing) {
        super(stage);
        this.pos = pos;
        this.facing = facing;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getEnumFacing() {
        return facing;
    }
}
