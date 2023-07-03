package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.awt.*;

public class BindButton extends Button {

    private final Setting<Bind>  setting;
    public boolean isListening;
    protected TimeAnimation anim;

    public BindButton(Setting<Bind> setting) {
        super(setting.getName());
        this.setting = setting;
        anim= new TimeAnimation(200, 0,94.4 * ForeverClientGui.getScale(),false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        anim.update();
        RenderUtil.drawRect(x, y, x + anim.getCurrentFloat(), y + height - 0.5f, getState() ? (!isHovering(mouseX, mouseY) ? ClickGui.getInstance().mainColor.getColor().getRGB() : ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue())) : !isHovering(mouseX, mouseY) ? 0x11555555 : 0x88555555);
        if (isListening) {
            textManager.drawStringWithShadow("Listening...", x + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
        } else {
            textManager.drawStringWithShadow(setting.getName() + " " + TextUtil.GRAY + GameSettings.getKeyDisplayString(setting.getValue().getKey()), x + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {
            String description =
                    ChatFormatting.GREEN +
                            "Value " +
                            ChatFormatting.WHITE +
                            setting.getValue() + "." +
                            "\n" +
                            ChatFormatting.WHITE +
                            (setting.getDescription().equals("") ? "A Bind setting." : setting.getDescription());
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
    public void updateVisibility() {
        anim.reset();
    }
    @Override
    public void update() {
        this.setHidden(!setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isListening) {
            setting.setValue(new Bind(mouseButton - 100));
            isListening = false;
        } else {
            if (isHovering(mouseX, mouseY) && mouseButton == 0) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                isListening = !isListening;
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (isListening) {
            Bind bind = new Bind(keyCode);
            if (bind.toString().equalsIgnoreCase("Escape")) {
                return;
            } else if (bind.toString().equalsIgnoreCase("Delete")) {
                bind = new Bind(0);
            }
            setting.setValue(bind);
            isListening = false;
//            super.onMouseClick();
        }
    }

    @Override
    public float getHeight() {
        return 12;
    }

//    public void toggle() {
//        anim.reset();
//        isListening = !isListening;
//    }

    public boolean getState() {
        return !isListening;
    }
}
