package we.devs.forever.mixin.mixins.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.util.client.CapeUtil;
import we.devs.forever.client.modules.impl.client.Cape;

import javax.annotation.Nullable;

@Mixin(value = {AbstractClientPlayer.class})
public abstract class MixinAbstractClientPlayer {
    @Shadow
    @Nullable
    protected abstract NetworkPlayerInfo getPlayerInfo();

    @Inject(method = {"getLocationCape"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void getLocationCape(CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {
        NetworkPlayerInfo info = this.getPlayerInfo();
        assert info != null;

        if (CapeUtil.isVip(info.getGameProfile().getId()) && Cape.cape.isEnabled()) {
            switch (Cape.cape.capeMode.getValue()) {
                case Forever:
                    callbackInfoReturnable.setReturnValue(new ResourceLocation("capes/cape.png"));
                    break;
                case Custom:
                    callbackInfoReturnable.setReturnValue(Cape.cape.image.getValue().getImage());
                    break;

            }
        }
    }

}
