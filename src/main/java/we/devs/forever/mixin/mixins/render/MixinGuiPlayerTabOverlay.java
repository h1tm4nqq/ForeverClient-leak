package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.client.modules.impl.misc.ExtraTab;

import java.util.List;

@Mixin(value = {GuiPlayerTabOverlay.class})
public class MixinGuiPlayerTabOverlay
        extends Gui {
    @Redirect(method = {"renderPlayerlist"}, at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public List<NetworkPlayerInfo> subListHook(List<NetworkPlayerInfo> list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, ExtraTab.getInstance().isEnabled() ? Math.min(ExtraTab.getInstance().size.getValue(), list.size()) : toIndex);
    }

    @Inject(method = {"getPlayerName"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void getPlayerNameHook(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> info) {
        if (ExtraTab.getInstance().isEnabled()) {
            info.setReturnValue(ExtraTab.getPlayerName(networkPlayerInfoIn));
        }
    }
}
