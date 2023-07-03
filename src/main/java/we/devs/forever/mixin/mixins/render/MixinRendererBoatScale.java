package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.modules.impl.movement.boatfly.BoatFly;

@Mixin(value = {ModelBoat.class})
public class MixinRendererBoatScale {
    @Inject(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBoat;setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V")})
    private void Method4445(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if ((BoatFly.INSTANCE.isEnabled() && entityIn == Minecraft.getMinecraft().player.getRidingEntity())) {
            double scale_ = BoatFly.INSTANCE.renderScale.getValue();
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && scale_ != 1.0) {
                GlStateManager.scale(scale_, scale_, scale_);
            }
        }
    }

    @Inject(method = {"renderMultipass"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;colorMask(ZZZZ)V", ordinal = 0)})
    private void Method4446(Entity entityIn, float partialTicks, float p_187054_3_, float p_187054_4_, float p_187054_5_, float p_187054_6_, float scale, CallbackInfo ci) {
        if (( BoatFly.INSTANCE.isEnabled() && entityIn == Minecraft.getMinecraft().player.getRidingEntity())) {
            double scale_ = BoatFly.INSTANCE.renderScale.getValue();
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && scale_ != 1.0) {
                GlStateManager.scale(scale_, scale_, scale_);
            }
        }
    }
}

