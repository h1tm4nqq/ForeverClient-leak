package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import we.devs.forever.client.modules.impl.render.ViewModel;

import java.awt.*;

@Mixin({RenderItem.class})
public class MixinRenderItem {
    @Shadow
    private void renderModel(final IBakedModel model, final int color, final ItemStack stack) {
    }


    @ModifyArg(method = {"renderEffect"}, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/RenderItem.renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;I)V"), index = 1)
    private int renderEffect(final int oldValue) {
        return ViewModel.getInstance().isEnabled() && ViewModel.getInstance().glintModify.getValue() ? ViewModel.getInstance().glintModifyColor.getColor().getRGB() : oldValue;
    }

    @Redirect(method = {"renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderItem;renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V"))
    private void renderModelColor(RenderItem renderItem, IBakedModel model, ItemStack stack) {
        if (ViewModel.getInstance().isEnabled() && ViewModel.getInstance().colors.getValue())
            this.renderModel(model, new Color(ViewModel.getInstance().red.getValue() / 255.0f, ViewModel.getInstance().green.getValue() / 255.0f, ViewModel.getInstance().blue.getValue() / 255.0f, ViewModel.getInstance().alpha.getValue() / 255.0f).getRGB(), stack);
        else {
            this.renderModel(model, new Color(255, 255, 255, 255).getRGB(), stack);
        }

    }


//    @Inject(method = {"renderItemModel"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", shift = At.Shift.BEFORE)})
//    private void renderItemModel(final ItemStack stack, final IBakedModel bakedModel, final ItemCameraTransforms.TransformType transform, final boolean leftHanded, final CallbackInfo ci) {
//        final RenderItemEvent event = new RenderItemEvent(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
//        ForeverClient.EVENT_BUS.post(event);
//        if (ViewModel.getInstance().isEnabled()) {
//            if (!leftHanded) {
//                GlStateManager.scale(event.getMainHandScaleX(), event.getMainHandScaleY(), event.getMainHandScaleZ());
//            } else {
//                GlStateManager.scale(event.getOffHandScaleX(), event.getOffHandScaleY(), event.getOffHandScaleZ());
//            }
//        }
//    }

}
