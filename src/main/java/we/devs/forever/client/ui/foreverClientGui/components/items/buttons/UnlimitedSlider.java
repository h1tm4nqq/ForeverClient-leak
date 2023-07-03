package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.awt.*;

public class UnlimitedSlider extends Button {
    public Setting setting;

    public UnlimitedSlider(final Setting setting) {
        super(setting.getName());
        this.setting = setting;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (ClickGui.getInstance().rainbowRolling.getValue()) {
            final int color = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) this.y, 0, this.textManager.scaledHeight)), moduleManager.getModuleByClass(ClickGui.class).mainColor.getColor().getAlpha());
            final int color2 = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) this.y + this.height, 0, this.textManager.scaledHeight)), moduleManager.getModuleByClass(ClickGui.class).mainColor.getColor().getAlpha());
            RenderUtil.drawGradientRect((float) (int) this.x, (float) (int) this.y, this.width + 7.4f, this.height, color, color2);
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + this.width + 7.4f, this.y + this.height - 0.5f, this.isHovering(mouseX, mouseY) ? ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue()) : ClickGui.getInstance().mainColor.getColor().getRGB());
        }
        textManager.drawStringWithShadow(" - " + this.setting.getName() + " " + "" + this.setting.getValue() + "" + " +", this.x + 2.3f, this.y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {

            String description =
                    ChatFormatting.GREEN +
                            "Value " +
                            ChatFormatting.WHITE +
                            "∞ - " + setting.getValue() + " - ∞" +
                            "." +
                            "\n" +
                            ChatFormatting.WHITE +
                            (setting.getDescription().equals("") ? "A Number setting." : setting.getDescription());
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
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            UnlimitedSlider.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (this.isRight(mouseX)) {
                if (this.setting.getValue() instanceof Double) {
                    this.setting.setValue((double) this.setting.getValue() + 1.0);
                } else if (this.setting.getValue() instanceof Float) {
                    this.setting.setValue((float) this.setting.getValue() + 1.0f);
                } else if (this.setting.getValue() instanceof Integer) {
                    this.setting.setValue((int) this.setting.getValue() + 1);
                }
            } else if (this.setting.getValue() instanceof Double) {
                this.setting.setValue((double) this.setting.getValue() - 1.0);
            } else if (this.setting.getValue() instanceof Float) {
                this.setting.setValue((float) this.setting.getValue() - 1.0f);
            } else if (this.setting.getValue() instanceof Integer) {
                this.setting.setValue((int) this.setting.getValue() - 1);
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public float getHeight() {
        return 12;
    }

    @Override
    public void toggle() {
    }

    @Override
    public boolean getState() {
        return true;
    }

    public boolean isRight(final int x) {
        return x > this.x + (this.width + 7.4f) / 2.0f;
    }
}
