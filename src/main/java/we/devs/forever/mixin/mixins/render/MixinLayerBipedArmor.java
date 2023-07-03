package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.modules.impl.render.NoRender;

@Mixin(LayerBipedArmor.class)
public abstract
class MixinLayerBipedArmor extends LayerArmorBase<ModelBiped> {

    public MixinLayerBipedArmor(RenderLivingBase<?> rendererIn) {
        super(rendererIn);
    }

    @Inject(method = "setModelSlotVisible*", at = @At("HEAD"), cancellable = true)
    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn, CallbackInfo info) {
        NoRender noArmor = NoRender.getInstance();
        if (noArmor.isEnabled() && noArmor.noArmor.getValue() != NoRender.NoArmor.None) {
            info.cancel();
            switch (slotIn) {
                case HEAD:
                    model.bipedHead.showModel = false;
                    model.bipedHeadwear.showModel = false;
                    break;
                case CHEST:
                    model.bipedBody.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    model.bipedRightArm.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    model.bipedLeftArm.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    break;
                case LEGS:
                    model.bipedBody.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    model.bipedRightLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    model.bipedLeftLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    break;
                case FEET:
                    model.bipedRightLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
                    model.bipedLeftLeg.showModel = noArmor.noArmor.getValue() != NoRender.NoArmor.All;
            }
        }
    }

}
