package we.devs.forever.mixin.mixins.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.event.events.world.BlockInteractEvent;
import we.devs.forever.main.ForeverClient;

@Mixin({Block.class})
public abstract class MixinBlock {

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState state, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> cir) {
        Block block = state.getBlock();
        BlockInteractEvent event = new BlockInteractEvent(block);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }
    }
}