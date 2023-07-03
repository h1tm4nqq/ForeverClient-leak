package we.devs.forever.api.event.events.player;


import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.EventStage;

public class BlockBreakingEvent extends EventStage {
    public BlockPos pos;
    public int breakingID;
    public int breakStage;

    public BlockBreakingEvent(final BlockPos pos, final int breakingID, final int breakStage) {
        this.pos = pos;
        this.breakingID = breakingID;
        this.breakStage = breakStage;
    }
}
