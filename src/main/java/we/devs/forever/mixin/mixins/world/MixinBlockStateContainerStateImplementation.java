package we.devs.forever.mixin.mixins.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockStateContainer.StateImplementation.class, priority = 2147483647)
public class MixinBlockStateContainerStateImplementation {
    // We have to use this cause old thingy is depricated
    @Shadow
    @Final
    private Block block;

//    @Redirect(method = "addCollisionBoxToList", at = @At(value="INVOKE", target = "Lnet/minecraft/block/Block;addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"))
//    public void addCollisionBoxToList(Block blk, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
//        BlockCollisionBoundingBoxEvent event = new BlockCollisionBoundingBoxEvent(blk, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
//        ForeverClient.EVENT_BUS.post(event);
//        if (!event.isCanceled())
//            block.addCollisionBoxToList(event.getState(), event.getWorld(), event.getPos(), event.getEntityBox(), event.getCollidingBoxes(), event.getEntity(), event.isActualState());
//    }
}