package we.devs.forever.mixin.mixins.render;


import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.client.modules.impl.chat.Chat;
import we.devs.forever.mixin.ducks.IChatLine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Mixin(value = {GuiNewChat.class})
public abstract class MixinGuiNewChat
        extends Gui {

    //    @Shadow
//    @Final
//    private List<ChatLine> drawnChatLines;
    @Shadow
    public abstract void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId);

    private String lastMessage;
    private int sameMessageAmount;
    private int line;
    ChatLine chatLine;
    ChatLine currentHover = null;
    private boolean first = true;

    @Redirect(method = {"drawChat"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V", ordinal = 0))
    private void drawRectHook(int left, int top, int right, int bottom, int color) {
        if (Chat.getInstance().isEnabled()) {
            switch (Chat.bgMode.getValue()) {
                case Custom:
                    color = Chat.bgColor.getColor().getRGB();
                    break;
                case Clean:
                    color = 0;
                    break;
            }
            Gui.drawRect(left,
                    top,
                    right + (Chat.timeStamps.getValue() ? 40 : 0),
                    bottom,
                    color);
        } else {
            Gui.drawRect(left,
                    top,
                    right,
                    bottom,
                    color);
        }


    }

    @Redirect(method = {"drawChat"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
        TimeAnimation animation = null;
        if (chatLine != null) {
            if (Chat.animationMap.containsKey(chatLine)) {
                animation = Chat.animationMap.get(chatLine);
            }
            if (animation != null) {
                animation.update();
            }
        }
        if (Chat.timeStamps.getValue() && Chat.getInstance().isEnabled()&& chatLine != null) {
            text = ((IChatLine) chatLine).getTimeStamp() + " " + text;
        }
        return fontRenderer.drawStringWithShadow(text, animation != null && Chat.animation.getValue() && Chat.getInstance().isEnabled() ? x + animation.getCurrentFloat() : x, y, color);
    }


    /**
     * @author хитманкуку
     * @reason ебал маму мудонны
     */
    @Overwrite
    public void printChatMessage(ITextComponent chatComponent) {
        if (Chat.getInstance().isEnabled()) {
            String text = fixString(chatComponent.getFormattedText());

            if (text.equals(this.lastMessage)) {
                (Minecraft.getMinecraft()).ingameGUI.getChatGUI().deleteChatLine(this.line);
                this.sameMessageAmount++;
                chatComponent.appendText(ChatFormatting.WHITE + " [" + "x" + this.sameMessageAmount + "]");
            } else {
                this.sameMessageAmount = 1;
            }

            this.lastMessage = text;
            this.line++;
            if (this.line > 256)
                this.line = 0;

            printChatMessageWithOptionalDeletion(chatComponent, this.line);
        } else {
            printChatMessageWithOptionalDeletion(chatComponent, 0);
        }
    }

    private String fixString(String str) {
        str = str.replaceAll("\uF8FF", "");//remove air chars

        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if ((int) c > (33 + 65248) && (int) c < (128 + 65248)) {
                sb.append(Character.toChars((int) c - 65248));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    @Redirect(method = "getChatComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ChatLine;getChatComponent()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent getHook(ChatLine chatLine) {
        currentHover = chatLine;
        return chatLine.getChatComponent();
    }

    @Inject(
            method = "getChatComponent",
            at = @At("HEAD"))
    public void getChatComponentHook(
            int mouseX,
            int mouseY,
            CallbackInfoReturnable<ITextComponent> info) {
        first = true;
    }

    @Redirect(method = "getChatComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"))
    public int getStringWidthHook(FontRenderer renderer, String text) {
        // Not sure anymore what the first is good for
        if (Chat.getInstance().isEnabled()
                && Chat.timeStamps.getValue()
                && currentHover != null) {
            first = false;

            return renderer.getStringWidth(text) + 9;
        }
        return renderer.getStringWidth(text);
    }

    @Redirect(method = "getChatComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;getChatWidth()I"))
    public int getChatComponentChatWidthHook(GuiNewChat gnc) {
        return Chat.getInstance().isEnabled() && Chat.timeStamps.getValue()
                ? gnc.getChatWidth() + 40
                : gnc.getChatWidth();
    }

    @Redirect(method = {"drawChat"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/FontRenderer;FONT_HEIGHT:I"))
    private int textHeight(FontRenderer instance) {
        return 9;
    }

    private static String getTimeString() {
        return "<" + new SimpleDateFormat("k:mm").format(new Date()) + ">";
    }

    @Redirect(method = {"setChatLine"}, at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0, remap = false))
    public int drawnChatLinesSize(List<ChatLine> list) {
        return Chat.getInstance().isEnabled() && Chat.infinite.getValue() ? -2147483647 : list.size();
    }

    @Redirect(method = {"setChatLine"}, at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2, remap = false))
    public int chatLinesSize(List<ChatLine> list) {
        return Chat.getInstance().isEnabled() && Chat.infinite.getValue() ? -2147483647 : list.size();
    }

    @Inject(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ChatLine;getUpdatedCounter()I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void drawChatHook(int updateCounter, CallbackInfo ci, int i, int j, float f, boolean flag, float f1, int k, int l, int il, ChatLine chatLine) {
        this.chatLine = chatLine;
    }
}