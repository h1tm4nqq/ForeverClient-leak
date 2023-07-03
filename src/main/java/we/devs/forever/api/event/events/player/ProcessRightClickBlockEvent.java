package we.devs.forever.api.event.events.player;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.event.EventStage;


public
class ProcessRightClickBlockEvent extends EventStage {

    public BlockPos pos;
    public EnumHand hand;
    public ItemStack stack;

    public ProcessRightClickBlockEvent(BlockPos pos, EnumHand hand, ItemStack stack) {
        super();
        this.pos = pos;
        this.hand = hand;
        this.stack = stack;
    }

}
