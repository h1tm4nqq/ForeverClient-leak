package we.devs.forever.client.ui.newGui.impl.frames;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.manager.impl.client.ModuleManager;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ModuleButton;
import we.devs.forever.client.ui.newGui.NewGui;
import we.devs.forever.client.ui.newGui.api.Beta;
import we.devs.forever.client.ui.newGui.api.Frame;

import java.awt.*;
import java.util.ArrayList;

public class ModuleFrame extends Frame {
    public Module getModule() {
        return module;
    }

    private final Beta beta;
    private final Module module;
    private final float widthBetaText;
    private final ArrayList<Frame<?>> frames = new ArrayList<>();

    private boolean open = false, openOld = false, isbeta = false;

    private TimeAnimation animation = new TimeAnimation(200, 0, 1);
    private TimeAnimation animationRev = new TimeAnimation(200, 0, 0);
    public TimeAnimation anim = new TimeAnimation(200, 0, 1);
    public TimeAnimation animRev = new TimeAnimation(200, -1, 0);

    @SuppressWarnings("unchecked")
    public ModuleFrame(Module module) {
        super(null);
        this.module = module;
        ModuleManager.getSettings(module).forEach(setting -> {
            if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                frames.add(new BooleanFrame((Setting<Boolean>) setting));
            }
            if (setting.getValue() instanceof Bind && !this.module.getName().equalsIgnoreCase("Hud")) {
                frames.add(new BindFrame((Setting<Bind>) setting));
            }
            if (setting.getValue() instanceof String || setting.getValue() instanceof Character) {
                frames.add(new StringFrame((Setting<String>) setting));
            }
            if (setting.getValue() instanceof Image) {
                frames.add(new ImageFrame((Setting<Image>) setting));
            }
            if (setting.isNumberSetting()) {
                //      if (setting.hasRestriction()) {
                setting.getClass().getAnnotation(Beta.class);
                frames.add(new NumberFrame((Setting<Number>) setting));
                //   }
                //    frames.add(new UnlimitedSlider(setting));
            }
            if (setting.isEnumSetting()) {
                frames.add(new EnumFrame((Setting<Enum<?>>) setting));
                return;
            }
            if (setting.isColorSetting()) {
                frames.add(new ColorFrame((Setting<Color>) setting));
            }
        });
        beta = module.getClass().getAnnotation(Beta.class);
        isbeta = beta != null;
        widthBetaText = NewGui.fontButtons.getStringWidth(module.getName());
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(getModule().isEnabled())  GuiRenderUtil.drawBorderedRectangle(x, y, width, 13,1f,
                isHovering(mouseX, mouseY)
                        ?  ColorUtil.changeAlpha(ClickGui.mainColor.getColor(),Math.abs(ClickGui.mainColor.getColor().getAlpha() - 70))
            : ClickGui.mainColor.getColor()
                ,new Color(0,0,0,0));

        NewGui.fontButtons.drawStringWithShadow(getModule().getName(),
                x + 3.5F,
                isHovering(mouseX, mouseY) ? 2F + y : 3F + y,  Color.WHITE.getRGB());

        if (isbeta) NewGui.fontBeta.drawStringWithShadow(beta.value(),
                x + widthBetaText + 5.8F,
                4 + y, beta.c().getColor());

        if (frames.isEmpty()) return;
        float current = open ? animation.getCurrentFloat() : -animationRev.getCurrentFloat();

        float l = (float) frames.stream()
                .filter(Frame::isVisible)
                .mapToDouble(frame -> frame.getHeight() + .5)
                .sum() + 13.5F;
        float l1 = (float) frames.stream()
                .mapToDouble(frame -> frame.getHeight() + .5)
                .sum() + 13.5F;
        float height = this.height;
        if (current != 0) {
            GuiRenderUtil.drawBorderedRectangle(
                    x,
                    y,
                    width,
                    (l - 1.5F) * current,
                    1,
                    new Color(0, 0, 0, 0),
                    ColorUtil.changeAlpha(ClickGui.mainColor.getColor(), (int) (ClickGui.mainColor.getColor().getAlpha() * current)));

            for (Frame<?> frame : frames) {
                if (frame.isVisible()) {
                    if (l1 * current <= height) {
                        break;
                    }
                    frame.setX(x);
                    frame.setY(y + height);

                    frame.drawScreen(mouseX, mouseY, partialTicks);
                    height += (frame.getHeight() + .5F);
                }

                frame.update();
            }
        }

        if (openOld != open) {
            updateVisibility();
            openOld = open;
        }
        if (open) animation.update();
        else animationRev.update();

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (frames.isEmpty()) return;
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            ModuleButton.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            module.toggle();
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;
            ModuleButton.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
        if (open)
            frames.stream().filter(Frame::isVisible).forEach(frame -> frame.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public float getHeight() {
        if (open) return (float) ((frames.stream()
                .filter(Frame::isVisible)
                .mapToDouble(frame -> frame.getHeight() + .5)
                .sum() + 13.5F) * anim.getCurrentFloat());
        else {
            float current = (float) ((frames.stream()
                    .filter(Frame::isVisible)
                    .mapToDouble(frame -> frame.getHeight() + .5)
                    .sum() + 13.5F) * -animRev.getCurrentFloat());
            return Math.max(13F, current);
        }
    }

    @Override
    public void updateVisibility() {
        animationRev = new TimeAnimation(200, -1, 0);
        animation = new TimeAnimation(200, 0, 1);
        frames.forEach(Frame::updateVisibility);
    }

    @Override
    public void update() {

    }

    @Override
    public void updateScreen() {
        frames.stream().filter(Frame::isVisible).forEach(Frame::updateScreen);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        frames.stream().filter(Frame::isVisible).forEach(frame -> frame.keyTyped(typedChar, keyCode));
    }

    public boolean isOpen() {
        return open;
    }

}
