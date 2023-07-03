package we.devs.forever.mixin.mixins.render;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.impl.chat.Chat;
import we.devs.forever.mixin.ducks.IChatLine;

import java.text.SimpleDateFormat;
import java.util.Date;

@Mixin(ChatLine.class)
public abstract class MixinChatLine implements IChatLine {
    private String timeStamp;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructorHook(int updateCounterCreatedIn, ITextComponent lineStringIn, int chatLineIDIn, CallbackInfo ci) {
        timeStamp = TextUtil.RAINBOW_MINUS + "<" + new SimpleDateFormat("k:mm").format(new Date()) + ">" + TextUtil.RESET;
        String t = lineStringIn.getFormattedText();
        if (Chat.timeStamps.getValue() && Chat.getInstance().isEnabled()) {
            t = timeStamp + " " + lineStringIn.getFormattedText();
        }

        Chat.animationMap.put(ChatLine.class.cast(this), new TimeAnimation(Chat.time.getValue(),
                -(Minecraft.getMinecraft().fontRenderer.getStringWidth(t)),
                0));

    }


    @Override
    public String getTimeStamp() {
        return timeStamp;
    }


}
