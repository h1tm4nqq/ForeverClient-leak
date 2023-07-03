package we.devs.forever.mixin.mixins.player;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.event.events.client.ClickWindowEvent;
import we.devs.forever.api.event.events.player.ProcessRightClickBlockEvent;
import we.devs.forever.api.event.events.player.ReachEvent;
import we.devs.forever.api.event.events.player.StopUsingItemEvent;
import we.devs.forever.api.event.events.world.BlockEvent;
import we.devs.forever.api.event.events.world.BlockResetEvent;
import we.devs.forever.main.ForeverClient;
import we.devs.forever.mixin.mixins.accessor.IPlayerControllerMP;

@Mixin({PlayerControllerMP.class})
public abstract class MixinPlayerControllerMP implements IPlayerControllerMP {

    @Shadow
    public int blockHitDelay;
    @Shadow
    public boolean isHittingBlock;
    @Shadow public abstract void syncCurrentPlayItem();

    @Redirect(method = "onPlayerDamageBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getPlayerRelativeBlockHardness(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F"))
    public float getPlayerRelativeBlockHardnessHook(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return state.getPlayerRelativeBlockHardness(player, worldIn, pos) * (/*TpsSync.getInstance().isEnabled() && TpsSync.getInstance().mining.getValue() ? 1 / nekoplus.serverManager.getTpsFactor() :*/ 1);
    }

    @Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
    private void onGetBlockReachDistance(final CallbackInfoReturnable<Float> cir) {
        final ReachEvent e = new ReachEvent(cir.getReturnValue());
        ForeverClient.EVENT_BUS.post(e);
        cir.setReturnValue(e.distance);
    }

    /*
        @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
        public
        void resetBlockRemovingHook ( CallbackInfo info ) {
            if (Speedmine.getInstance().isEnabled() && Speedmine.getInstance().reset.getValue()) {
                info.cancel();
            }
        }

     */
    @Inject(method = "windowClick", at = @At("RETURN"))
    private void onWindowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        ClickWindowEvent event = new ClickWindowEvent();
        ForeverClient.EVENT_BUS.post(event);
    }


    @Inject(method = "onStoppedUsingItem", at = @At("HEAD"), cancellable = true)
    private void onStoppedUsingItemInject(EntityPlayer playerIn, CallbackInfo ci) {
        if (playerIn.equals(Minecraft.getMinecraft().player)) {
            StopUsingItemEvent event = new StopUsingItemEvent();
            ForeverClient.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                if (event.isPacket()) {
                    this.syncCurrentPlayItem();
                    playerIn.stopActiveHand();
                }
                ci.cancel();
            }
        }
    }
//    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
//    private void onPlayerDamageBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
//       ForeverClient.EVENT_BUS.post(new BlockEvent(4, pos, face));
//    }

//    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
//    private void onPlayerDamageBlock(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
//        BlockEvent event = new BlockEvent(4, pos, face);
//
//        ForeverClient.EVENT_BUS.post(event);
//
//        if (event.isCanceled()) {
//            info.cancel();
//        }
//    }

    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    private void clickBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        BlockEvent event = new BlockEvent(4, posBlock, directionFacing);

        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.cancel();
        }
    }


    @Inject(method = "resetBlockRemoving", at = @At(value = "HEAD"), cancellable = true)
    private void onResetBlockRemoving(CallbackInfo info) {
        BlockResetEvent blockResetEvent = new BlockResetEvent();
        ForeverClient.EVENT_BUS.post(blockResetEvent);
        if (blockResetEvent.isCanceled()) {
            info.cancel();
        }
    }

/*
    @Inject(method = "clickBlock", at = @At("HEAD"), cancellable = true)
    private void clickBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        BlockEvent event = new BlockEvent(3, pos, face);
       ForeverClient.EVENT_BUS.post(event);
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamageBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        BlockEvent event = new BlockEvent(4, pos, face);
       ForeverClient.EVENT_BUS.post(event);
    }
 */

    @Inject(method = "getBlockReachDistance", at = @At("RETURN"), cancellable = true)
    private void getReachDistanceHook(CallbackInfoReturnable<Float> distance) {
        /*if(Reach.getInstance().isEnabled()) {
            float range = distance.getReturnValue();
            distance.setReturnValue(Reach.getInstance().override.getValue() ? Reach.getInstance().reach.getValue() : range + Reach.getInstance().add.getValue());
        }*/
    }

    @Redirect(method = "processRightClickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemBlock;canPlaceBlockOnSide(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean canPlaceBlockOnSideHook(ItemBlock itemBlock, World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos)) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(side);
        }

        IBlockState iblockstate1 = worldIn.getBlockState(pos);
        AxisAlignedBB axisalignedbb = itemBlock.block.getDefaultState().getCollisionBoundingBox(worldIn, pos);
        if (axisalignedbb != Block.NULL_AABB && !worldIn.checkNoEntityCollision(axisalignedbb.offset(pos), null)) {
            return false;
        } else if (iblockstate1.getMaterial() == Material.CIRCUITS && itemBlock.block == Blocks.ANVIL) {
            return true;
        }

        return iblockstate1.getBlock().isReplaceable(worldIn, pos) && itemBlock.block.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Inject(method = "processRightClickBlock", at = @At("HEAD"), cancellable = true)
    public void processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        ProcessRightClickBlockEvent event = new ProcessRightClickBlockEvent(pos, hand, Minecraft.getMinecraft().player.getHeldItem(hand));
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.cancel();
        }
    }


}
