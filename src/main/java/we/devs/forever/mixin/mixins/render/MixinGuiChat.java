package we.devs.forever.mixin.mixins.render;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.Client;
import we.devs.forever.client.ui.chat.ForeverGuiChat;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Shadow protected GuiTextField inputField;

    @Inject(method = "keyTyped", at = @At("RETURN"))
    public void konasChatInject(char charTyped, int keyCode, CallbackInfo ci) {

        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiChat) || Minecraft.getMinecraft().currentScreen instanceof ForeverGuiChat) return;

        if(inputField.getText().replaceAll(" ", "").startsWith(Client.commandManager.getPrefix())) {
            Minecraft.getMinecraft().displayGuiScreen(new ForeverGuiChat(inputField.getText()));
        }

    }

}
