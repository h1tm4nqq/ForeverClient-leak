package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.Component;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;

public class Slider extends Button {

    private final Number min;
    private final Number max;
    private final int difference;
    public Setting setting;
    boolean isFloat;
    public boolean isListening;
    private String currentString = "";


    public Slider(Setting setting) {
        super(setting.getName());
        isFloat = setting.isFloat();
        this.setting = setting;
        this.min = (Number) setting.getMin();
        this.max = (Number) setting.getMax();
        this.difference = max.intValue() - min.intValue();
        currentString = setting.getValueAsString();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragSetting(mouseX, mouseY);
        RenderUtil.drawRect(x, y, x + width + 7.4F, y + height - 0.5f, !isHovering(mouseX, mouseY) ? 0x11555555 : 0x88555555);
        RenderUtil.drawRect(x, y, ((Number) setting.getValue()).floatValue() <= min.floatValue() ? x : x + (width + 7.4F) * partialMultiplier(), y + height - 0.5f, !isHovering(mouseX, mouseY) ? ClickGui.getInstance().mainColor.getColor().getRGB() : ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue()));

        if (isListening)
            textManager.drawStringWithShadow(getName() + " " + TextUtil.GRAY + currentString + Client.textManager.getIdleSign(), x + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), 0xFFFFFFFF);
        else
            textManager.drawStringWithShadow(getName() + " " + TextUtil.GRAY + setting.getValue(), x + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), 0xFFFFFFFF);

        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {

            String description =
                    ChatFormatting.GREEN +
                            "Value " +
                            ChatFormatting.WHITE +
                           setting.getMin() +" - " + setting.getValue() + " - " + setting.getMax() +
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
    public void LeftClick(int mouseX, int mouseY) {
        setSettingFromX(mouseX);
    }

    @Override
    public void RightClick(int mouseX, int mouseY) {
        isListening = !isListening;
    }

    @Override
    public void onKeyTyped(final char typedChar, final int keyCode) {
        if (isListening) {
            if (keyCode == 1) {
                return;
            }
            if (keyCode == Keyboard.KEY_RETURN) {
                enterString();
            } else if (keyCode == Keyboard.KEY_BACK) {
                setString(removeLastChar(currentString));
            } else {
                if (keyCode == Keyboard.KEY_V && (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))) {
                    try {
                        setString(currentString + Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    setString(currentString + typedChar);
                }
            }
        }
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return mouseX >= getX() && mouseX <= getX() + getWidth() + 8 && mouseY >= getY() && mouseY <= getY() + height;
    }

    @Override
    public void update() {
        this.setHidden(!setting.isVisible());
    }

    private void dragSetting(int mouseX, int mouseY) {
        if (isHovering(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            setSettingFromX(mouseX);
        }
    }

    public static String removeLastChar(final String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public float getHeight() {
        return 12;
    }

    private void setSettingFromX(int mouseX) {
        float percent = (mouseX - x) / (width + 7.4F);
        if (setting.getValue() instanceof Double) {
            double result = (Double) setting.getMin() + (difference * percent);
            setting.setValue(Math.round(10.0 * result) / 10.0);
            setString(setting.getValue().toString());

        } else if (setting.getValue() instanceof Float) {
            float result = (Float) setting.getMin() + (difference * percent);
            setting.setValue(Math.round(10.0f * result) / 10.0f);
            setString(setting.getValue().toString());

        } else if (setting.getValue() instanceof Integer) {
            setting.setValue(((Integer) setting.getMin() + (int) (difference * percent)));
            setString(setting.getValue().toString());
        }
    }

    private void enterString() {
        try {
            if (setting.getValue() instanceof Integer) {
                int temp = Integer.parseInt(currentString);
                if (temp < (int) setting.getMin()) {
                    temp = (int) setting.getMin();
                } else if (temp > (int) setting.getMax()) {
                    temp = (int) setting.getMax();
                }
                setting.setValue(temp);
            } else if (setting.getValue() instanceof Float) {
                float temp = Float.parseFloat(currentString);
                if (temp < (float) setting.getMin()) {
                    temp = (float) setting.getMin();
                } else if (temp > (float) setting.getMax()) {
                    temp = (float) setting.getMax();
                }
                setting.setValue(temp);
            } else if (setting.getValue() instanceof Double) {
                double temp = Double.parseDouble(currentString);
                if (temp < (double) setting.getMin()) {
                    temp = (double) setting.getMin();
                } else if (temp > (double) setting.getMax()) {
                    temp = (double) setting.getMax();
                }
                setting.setValue(temp);
            }
        } catch (Throwable ignored) {
        }

        setString(setting.getValue().toString());
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        isListening = !isListening;
    }

    public void setString(final String newString) {
        currentString = newString;
    }

    private float middle() {
        return max.floatValue() - min.floatValue();
    }

    private float part() {
        return ((Number) setting.getValue()).floatValue() - min.floatValue();
    }

    private float partialMultiplier() {
        return part() / middle();
    }
}
