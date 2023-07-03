package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;

public class StringButton extends Button {
    private final Setting<String> setting;
    public boolean isListening;
    private CurrentString currentString;
    TimeAnimation animation  = new TimeAnimation(250, 0,94.4 * ForeverClientGui.getScale(),false);
    public StringButton(final Setting<String> setting) {
        super(setting.getName());
        this.currentString = new CurrentString("");
        this.setting = setting;

    }

    @Override
    public void updateVisibility() {
        animation.reset();
    }

    public static String removeLastChar(final String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        animation.update();
        if (ClickGui.getInstance().rainbowRolling.getValue()) {
            final int color = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) y, 0, textManager.scaledHeight)), moduleManager.getModuleByClass(ClickGui.class).mainColor.getColor().getAlpha());
            final int color2 = ColorUtil.changeAlpha(colorMap.get(MathUtil.clamp((int) y + height, 0, textManager.scaledHeight)), moduleManager.getModuleByClass(ClickGui.class).mainColor.getColor().getAlpha());
            RenderUtil.drawGradientRect(x, y, width + 7.4f, height - 0.5f, getState() ? (isHovering(mouseX, mouseY) ? color : colorMap.get(MathUtil.clamp((int) y, 0, textManager.scaledHeight))) : (isHovering(mouseX, mouseY) ? -2007673515 : 290805077), getState() ? (isHovering(mouseX, mouseY) ? color2 : colorMap.get(MathUtil.clamp((int) y + height, 0, textManager.scaledHeight))) : (isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        } else {
            RenderUtil.drawRect(x, y, x + animation.getCurrentFloat(), y + height - 0.5f, getState() ? (isHovering(mouseX, mouseY) ? ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue()) : ClickGui.getInstance().mainColor.getColor().getRGB()) : (isHovering(mouseX, mouseY) ? -2007673515 : 290805077));
        }
        if (isListening) {
            textManager.drawStringWithShadow(setting.getName() + ": " + currentString.getString() + Client.textManager.getIdleSign(), x + 2.3f, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), getState() ? -1 : -5592406);
        } else {
            textManager.drawStringWithShadow(setting.getName() + ": " + setting.getValue(), x + 2.3f, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), getState() ? -1 : -5592406);
        }
        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {
            String description =
                    ChatFormatting.GREEN +
                            "Value " +
                            ChatFormatting.WHITE +
                            "«" + setting.getValue() + "»" +
                            "." +
                            "\n" +
                            ChatFormatting.WHITE +
                            (setting.getDescription().equals("") ? "A String setting." : setting.getDescription());
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
        if (isHovering(mouseX, mouseY)) {
            StringButton.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public void onKeyTyped(final char typedChar, final int keyCode) {
        if (isListening) {
            if (keyCode == 1) {
                return;
            }
            if (keyCode == 28) {
                enterString();
            } else if (keyCode == 14) {
                setString(removeLastChar(currentString.getString()));
            } else {
                Label_0122:
                {
                    if (keyCode == 47) {
                        if (!Keyboard.isKeyDown(157)) {
                            if (!Keyboard.isKeyDown(29)) {
                                break Label_0122;
                            }
                        }
                        try {
                            setString(currentString.getString() + Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    setString(currentString.getString() + typedChar);
                }
            }
        }
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    private void enterString() {
        if (currentString.getString().isEmpty()) {
            setting.setValue(setting.getDefaultValue());
        } else {
            setting.setValue(currentString.getString());
        }
        setString("");
        super.onMouseClick();
    }

    @Override
    public float getHeight() {
        return 12;
    }

    @Override
    public void toggle() {
        isListening = !isListening;
    }

    @Override
    public boolean getState() {
        return !isListening;
    }

    public void setString(final String newString) {
        currentString = new CurrentString(newString);
    }

    public static class CurrentString {
        private final String string;

        public CurrentString(final String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }
}
