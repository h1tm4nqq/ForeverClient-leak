package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;


import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.EnumConverter;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.awt.*;

public class ColorPickerButton extends Button {
    private final Setting<Color> setting;
    private final float svPickerWidth;
    private final float hPickerWidth;
    private final float aPickerWidth;
    private final float hCursorX;
    boolean changed = false;
    float scale = ForeverClientGui.getScale();
    float h = 0;
    float rainbow = 0;
    private boolean open;
    private int speed;
    Mode colorMode = Mode.Normal;
    // positions
    private float svPickerX;
    private float svPickerY;
    private float svPickerHeight = 0;
    private float hPickerX;
    private float hPickerY;
    private float hPickerHeight = 0;
    private float aPickerX;
    private float aPickerY;
    private float aPickerHeight = 0;
    // cursor
    private float svCursorX, svCursorY = 0;
    private float hCursorY = 0;
    private float aCursorX, aCursorY = 0;
    private boolean svChanging, hChanging, aChanging, sChanging;
    float posYRainbow;
    public ColorPickerButton(Setting<Color> setting) {
        super(setting.getName());

        this.setting = setting;
        this.height = 12F;
        colorMode = setting.getColorMode();
        speed = setting.getSpeedColor();

        // size
        this.svPickerWidth = 80 * scale;
        this.svPickerHeight = 80 * scale;

        this.hPickerWidth = 6 * scale;
        this.hPickerHeight = svPickerHeight;

        this.aPickerWidth = svPickerWidth + hPickerWidth + 1;
        this.aPickerHeight = 6 * scale;

        // cursors
        Color c = this.setting.getValue();
        float[] hsv = Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), null);
        hCursorX = hPickerWidth / 2;
        hCursorY = hsv[0] * hPickerHeight;

        svCursorX = hsv[1] * svPickerWidth;
        svCursorY = (1.0F - hsv[2]) * svPickerHeight;

        aCursorX = (c.getAlpha() / 255.0F) * aPickerWidth;
        aCursorY = aPickerHeight / 2;
    }

    public float getCenter(float a, float b, float c) {
        return a + (b - c) / 2;
    }

    @Override
    public void update() {
        this.setHidden(!setting.isVisible());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        RenderUtil.drawRect(x, y, x + width + 7.4F, y + height - 0.5f, (!isHovering(mouseX, mouseY) ? ClickGui.getInstance().mainColor.getColor().getRGB() : ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue())));
        textManager.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 1.965f - ForeverClientGui.getClickGui().getTextOffset(), new Color(255, 255, 255, 255).getRGB());
        float rectScale = 13 * scale;
        float rectX = x + width - rectScale - 17;
        int rectY = (int) getCenter(y, height, rectScale);
        float[] offsets = new float[]{
                12.9F,
                0.9F,
                22.2F,
                -0.8F
        };
        RenderUtil.drawRect(rectX + offsets[0], rectY + offsets[1], rectX + rectScale + offsets[2], rectY + rectScale + offsets[3], new Color(63, 63, 64));
        RenderUtil.drawRect(rectX + 0.7F + offsets[0], rectY + 0.7F + offsets[1], rectX + rectScale - 0.7F + offsets[2], rectY + rectScale - 0.7F + offsets[3], new Color(17, 17, 17));
        RenderUtil.drawRect(rectX + 1.5F + offsets[0], rectY + 1.5F + offsets[1], rectX + rectScale - 1.5F + offsets[2], rectY + rectScale - 1.5F + offsets[3], new Color(63, 63, 64, 255));
        RenderUtil.drawRect(rectX + 1.5F + offsets[0], rectY + 1.5F + offsets[1], rectX + rectScale - 1.5F + offsets[2], rectY + rectScale - 1.5F + offsets[3], setting.getColor());
        if (open) {
            Color c = this.setting.getColor();
            float pickerX = this.x++;
            float pickerY = this.y + this.height;
            // sv
            h = (hPickerHeight - hCursorY) / hPickerHeight; //0-80
            float r;
            float[] offsets1 = new float[]{
                    -10.0F,
                    -0.3F,
                    -2.4F,
                    -0.3F,
                    0.2F,
                    -2.6F,
                    0.0F
            };
            this.svPickerX = pickerX + 12 * scale + offsets1[0];
            this.svPickerY = pickerY + 5 * scale + offsets1[1];
            r = 1.0F / svPickerHeight;
            for (int i = 0; i < svPickerHeight; i++) {
                float v0 = r * (svPickerHeight - i);
                float v1 = r * (svPickerHeight - (i + 1));
                int left = Color.HSBtoRGB(h, 0F, v0);
                int right = Color.HSBtoRGB(h, 1.0F, v1);
                RenderUtil.drawGradientRect(svPickerX, svPickerY + i, svPickerX + svPickerWidth,
                        svPickerY + i + 1.1F, left, right, left, right);
            }

            // sv cursor
            float svCx = svCursorX + svPickerX;
            float svCy = svCursorY + svPickerY;
            renderCursor(svCx, svCy);
            if (svChanging) {
                svCursorX = mouseX - svPickerX;
                if (svCursorX < 0)
                    svCursorX = 0;
                if (svCursorX > svPickerWidth)
                    svCursorX = svPickerWidth;

                svCursorY = mouseY - svPickerY;
                if (svCursorY < 0)
                    svCursorY = 0;
                if (svCursorY > svPickerHeight)
                    svCursorY = svPickerHeight;

                changed = true;
            }

            // h
            this.hPickerX = svPickerX + svPickerWidth + 8 + offsets1[2];
            this.hPickerY = svPickerY + offsets1[3];
            r = 1.0F / hPickerHeight;
            for (int i = 0; i < hPickerHeight; i++) {
                float h0 = r * (hPickerHeight - i);
                float h1 = r * (hPickerHeight - (i + 1));
                int top = Color.HSBtoRGB(h0, 1.0F, 1.0F);
                int bottom = Color.HSBtoRGB(h1, 1.0F, 1.0F);
                RenderUtil.drawGradientRect(hPickerX, hPickerY + i, hPickerX + hPickerWidth, hPickerY + i + 1, top,
                        top, bottom, bottom);
            }
            // h cursor
            renderCursorNoWith(hCursorX + hPickerX, hCursorY + hPickerY);
            if (hChanging && colorMode.equals(Mode.Normal)) {
//                    hCursorX = mouseX - hPickerX;
//                    if (hCursorX < 0)
//                        hCursorX = 0;
//                    if (hCursorX > hPickerWidth)
//                        hCursorX = hPickerWidth;

                hCursorY = mouseY - hPickerY;
                if (hCursorY < 0)
                    hCursorY = 0;
                if (hCursorY > hPickerHeight)
                    hCursorY = hPickerHeight;

                changed = true;
            }
            if (colorMode.equals(Mode.Rainbow)) {
                hCursorY = hPickerHeight - rainbow * hPickerHeight;
            }
            if(!setting.hide) {
                // alpha
                this.aPickerX = svPickerX + offsets1[4];
                this.aPickerY = svPickerY + svPickerHeight + 7 + offsets1[5];
                // rect
                float aRectScale = aPickerHeight / 2;
                float rectCount = aPickerWidth / aRectScale;
                for (int i = 0; i < rectCount; i++) {
                    float aRectX1 = aPickerX + i * aRectScale;
                    float aRectX2 = aPickerX + (i + 1) * aRectScale;
                    if (aRectX2 > aPickerX + aPickerWidth)
                        aRectX2 = aPickerX + aPickerWidth;

                    RenderUtil.drawRect(aRectX1, aPickerY, aRectX2 + offsets1[6], aPickerY + aRectScale,
                            i % 2 == 0 ? new Color(255, 255, 255) : new Color(204, 204, 204));
                    RenderUtil.drawRect(aRectX1, aPickerY + aRectScale, aRectX2 + offsets1[6], aPickerY + aRectScale * 2,
                            (i + 1) % 2 == 0 ? new Color(255, 255, 255) : new Color(204, 204, 204));
                }

                int right = ColorUtil.toRGBA(c.getRed(), c.getGreen(), c.getBlue(), 1);
                int left = ColorUtil.toRGBA(c.getRed(), c.getGreen(), c.getBlue(), 255);
                RenderUtil.drawGradientRect(aPickerX, aPickerY, aPickerX + aPickerWidth, aPickerY + aPickerHeight,
                        right, left, right, left);
                // cursor
                float aCx = aCursorX + aPickerX;
                float aCy = aCursorY + aPickerY;
                renderCursorNoHeight(aCx, aCy);
                if (aChanging) {
                    aCursorX = mouseX - aPickerX;
                    if (aCursorX < 0)
                        aCursorX = 0;
                    if (aCursorX > aPickerWidth)
                        aCursorX = aPickerWidth;
//                    aCursorY = mouseY - aPickerY;
//                    if (aCursorY < 0)
//                        aCursorY = 0;
//                    if (aCursorY > aPickerHeight)
//                        aCursorY = aPickerHeight;

                    changed = true;
                }
            }
            if (changed || colorMode == Mode.Rainbow) {
                rainbow = RenderUtil.getHui((256 - speed) * 100);
                float s = svCursorX / svPickerWidth;
                float v = (svPickerHeight - svCursorY) / svPickerHeight;
                float a = aCursorX / aPickerWidth;
                Color color = new Color(Color.HSBtoRGB(h, s, v));

                setting.setColor(s, v, new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (a * 255.0F)));

                changed = false;
            }
            posYRainbow = !setting.hide ? aPickerY + 8.5F: svPickerY + svPickerHeight + 1.5F;

            RenderUtil.drawRect(x, posYRainbow, x + width + 7.4F, posYRainbow + height,  (!isHovering(mouseX, mouseY, x, posYRainbow, x + width + 7.4F, posYRainbow + height) ? ClickGui.getInstance().mainColor.getColor().getRGB() : ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue())));
            textManager.drawStringWithShadow("ColorMod " + colorMode.name() , x + 2.3F, posYRainbow - 1.65F - ForeverClientGui.getClickGui().getTextOffset(),  0xFFFFFFFF);
            if (sChanging) {
                setSettingFromX(mouseX);
            }
            if (colorMode == Mode.Rainbow) {
                RenderUtil.drawRect(x, posYRainbow + 13F * scale, x + width + 7.4F,  height + posYRainbow + 13F * scale, !isHovering(mouseX, mouseY, x, posYRainbow + 13F * scale, x + width + 7.4F, posYRainbow + 13F * scale) ? 0x11555555 : 0x88555555);
                RenderUtil.drawRect(x, posYRainbow + 13F * scale, speed <= 1 ? x : x + (width + 7.4F) * partialMultiplier(),  height + posYRainbow + 13F * scale, !isHovering(mouseX, mouseY, x, posYRainbow + 13F * scale, x + width + 7.4F,  height + posYRainbow + 13F * scale) ? ClickGui.getInstance().mainColor.getColor().getRGB() : ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), ClickGui.getInstance().hoverAlpha.getValue()));
                textManager.drawStringWithShadow("Speed " + TextUtil.GRAY + speed, x + 2.3F, posYRainbow  + 13F * scale - 1.65F- ForeverClientGui.getClickGui().getTextOffset(), 0xFFFFFFFF);
                setting.setSpeedColor(256 - speed);
            }
            setting.setMode(colorMode);
            if (ClickGui.getInstance().desc.getValue() && isHovering(mouseX, mouseY)) {
                String description = setting.getDescription().equals("") ? "A Color setting." : setting.getDescription();
                ForeverClientGui.setDesc(() -> {
                    Gui.drawRect(mouseX + 6, mouseY + 11, mouseX + 6 + textManager.getStringWidth(description), mouseY, new Color(0, 0, 0, 190).getRGB());
                    textManager.drawStringWithShadow(description, mouseX + 6, mouseY, Color.WHITE.getRGB());
                });
            }
        }
    }

    @Override
    public float getHeight() {
        if (open) {
            float l = !setting.hide ? 135F :135F- 12.9F;
            return colorMode.equals(Mode.Rainbow) ? l : l - height;
        } else {
            return 12F;
        }
    }

    private void setSettingFromX(int mouseX) {
        float percent = (mouseX - x) / (width + 7.4F);
        int speed = 1 + (int) (254 * percent);
        if (speed >= 1 && speed <= 255) {
            this.speed = speed;
        }
    }

    private float partialMultiplier() {
        return (speed - 1F) / 254F;
    }

    private void renderCursor(float x, float y) {
        RenderUtil.drawRect(x - 2, y - 2, x + 2, y + 2, new Color(20, 20, 20));
        RenderUtil.drawRect(x - 1F, y - 1F, x + 1F, y + 1F, new Color(250, 250, 250));
    }

    private void renderCursorNoWith(float x, float y) {
        RenderUtil.drawRect(x - 6, y - 2, x + 6, y + 2, new Color(20, 20, 20));
        RenderUtil.drawRect(x - 5F, y - 1F, x + 5F, y + 1F, new Color(250, 250, 250));
    }

    private void renderCursorNoHeight(float x, float y) {
        RenderUtil.drawRect(x - 2, y - 6, x + 2, y + 6, new Color(20, 20, 20));
        RenderUtil.drawRect(x - 1F, y - 5F, x + 1F, y + 5F, new Color(250, 250, 250));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        if (open) {
            if(clickedButton == 0) {
                if (isHovering(mouseX, mouseY, svPickerX, svPickerY, svPickerX + svPickerWidth, svPickerY + svPickerHeight) && !colorMode.equals(Mode.Sync)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    svChanging = true;
                }
                if (isHovering(mouseX, mouseY, hPickerX, hPickerY, hPickerX + hPickerWidth, hPickerY + hPickerHeight) && colorMode.equals(Mode.Normal)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    hChanging = true;
                }
                if (isHovering(mouseX, mouseY, aPickerX, aPickerY, aPickerX + aPickerWidth, aPickerY + aPickerHeight) && !setting.hide) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    aChanging = true;
                }
                if (isHovering(mouseX, mouseY, x, posYRainbow+ 13F * scale, x + width + 7.4F, height+  posYRainbow+ 13F * scale) && colorMode.equals(Mode.Rainbow)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    sChanging = true;
                }
                if (isHovering(mouseX, mouseY, x, posYRainbow , x+ width + 7.4F, posYRainbow + height)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    colorMode = (Mode) EnumConverter.next(colorMode);
                    if(setting.hide && colorMode.equals(Mode.Sync)) colorMode = (Mode) EnumConverter.next(colorMode);
                }
            }
            if(clickedButton == 1) {
                if (isHovering(mouseX, mouseY, x, posYRainbow , x+ width + 7.4F, posYRainbow + height)) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    colorMode = (Mode) EnumConverter.previous(colorMode);
                    if(setting.hide && colorMode.equals(Mode.Sync)) colorMode = (Mode) EnumConverter.previous(colorMode);
                }
            }


        }
        if (isHovering(mouseX, mouseY) && setting.isVisible() && clickedButton == 1) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            open = !open;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (open) {
            svChanging = false;
            hChanging = false;
            aChanging = false;
            sChanging = false;
        }
    }


    public enum Mode{
        Normal,
        Rainbow,
        Sync
    }
}
