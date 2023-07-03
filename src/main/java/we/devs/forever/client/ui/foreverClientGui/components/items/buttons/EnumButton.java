package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.awt.*;

public class EnumButton extends Button {
    public Setting setting;
    TimeAnimation anim = new TimeAnimation(200,0,94.4 * ForeverClientGui.getScale());

    public EnumButton(final Setting setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void updateVisibility() {
        anim.reset();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {

        if (ClickGui.getInstance().rainbowRolling.getValue()) {
            final int color = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) this.y, 0, this.textManager.scaledHeight)), moduleManager.getModuleByClass(ClickGui.class).mainColor.getColor().getAlpha());
            final int color2 = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.textManager.scaledHeight)), moduleManager.getModuleByClass(ClickGui.class).mainColor.getColor().getAlpha());
            RenderUtil.drawGradientRect(this.x, this.y, this.width + 7.4f, this.height - 0.5f, this.getState() ? (this.isHovering(mouseX, mouseY) ? color : colorMap.get(MathUtil.clamp((int) this.y, 0, this.textManager.scaledHeight))) : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077), this.getState() ? (this.isHovering(mouseX, mouseY) ? color2 : colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.textManager.scaledHeight))) : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + anim.getCurrentFloat(), this.y + this.height - 0.5f, this.getState() ? (this.isHovering(mouseX, mouseY) ? ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue()) : ClickGui.getInstance().mainColor.getColor().getRGB()) : (this.isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        }
        textManager.drawStringWithShadow(setting.getName() + " " + TextUtil.GRAY + setting.currentEnumName(), x + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {
            String description =
                    ChatFormatting.GREEN +
                            "Value " +
                            ChatFormatting.WHITE +
                            setting.getValue() + "." +
                            "\n" +
                            ChatFormatting.WHITE +
                            (setting.getDescription().equals("") ? "A Mode/Page setting." : setting.getDescription());
            ForeverClientGui.setDesc(() -> {
                int i = 0;
                int width = 0;

                String[] descs = description.split("\n");
                for (String des : descs) {
                    i += textManager.getFontHeight() + 2;
                    int temp = textManager.getStringWidth(des);
                    if (temp > width) {
                        width = temp;
                    }
                }

                GuiRenderUtil.drawBorderedRect(mouseX + 6, mouseY - 2, mouseX + width + 9, mouseY + i, 1F, new Color(0, 0, 0, 190).getRGB(), new Color(0, 0, 0, 255).getRGB());
                i = 0;
                for (String des : descs) {
                    textManager.drawStringWithShadow(des, mouseX + 8, mouseY + i, Color.WHITE.getRGB());
                    i += textManager.getFontHeight() + 2;
                }

            });
        }
        anim.update();
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }


    @Override
    public void LeftClick(int mouseX, int mouseY) {
        anim.reset();
        setting.increaseEnum();
        EnumButton.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));

    }

    @Override
    public void RightClick(int mouseX, int mouseY) {
        anim.reset();
        setting.notincreaseEnum();
        EnumButton.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public float getHeight() {
        return 12;
    }


    @Override
    public boolean getState() {
        return true;
    }
}
