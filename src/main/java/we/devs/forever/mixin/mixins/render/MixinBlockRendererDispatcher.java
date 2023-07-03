package we.devs.forever.mixin.mixins.render;


import net.minecraft.client.renderer.BlockRendererDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockRendererDispatcher.class)
public class MixinBlockRendererDispatcher {

//    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
//    public void blockRenderInject(IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder bufferBuilderIn, CallbackInfoReturnable<Boolean> cir) {
//        if (Search.INSTANCE.isEnabled()) {
//            BlockRenderEvent event = new BlockRenderEvent(state.getBlock(), pos);
//            ForeverClient.EVENT_BUS.post(event);
//            if (event.isCanceled())
//                cir.setReturnValue(false);
//        }
//
//    }

}
