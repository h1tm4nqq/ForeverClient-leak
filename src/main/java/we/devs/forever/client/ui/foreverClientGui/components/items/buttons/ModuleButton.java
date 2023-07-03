package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.manager.impl.client.ModuleManager;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.items.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Button {
    private final Module module;
    private List<Item> items;
    private boolean subOpen;
    private boolean subOpenOld;
    boolean flag;
    TimeAnimation anim;
    TimeAnimation test= new TimeAnimation(250,0,height);
    TimeAnimation animGui = new TimeAnimation(150,0,13F * ForeverClientGui.getScale());

    public ModuleButton(final Module module) {
        super(module.getName());
        this.items = new ArrayList<>();
        this.module = module;
        flag = false;
        try {
            this.initSettings();
        } catch (NullPointerException nullPointerException){
            System.out.println(module.getName());
        }

        anim= new TimeAnimation(150, 0,190,false);
    }
    @SuppressWarnings("unchecked")
    public void initSettings() {
        final List<Item> newItems = new ArrayList<>();
        if(module == null) return;
        for (final Setting setting : ModuleManager.getSettings(module)) {
//                if(setting == null) {
//                    continue;
//                }
            // ForeverClient.LOGGER.info(module.getName());
            if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                newItems.add(new BooleanButton(setting));
            }
            if (setting.getValue() instanceof Bind && !this.module.getName().equalsIgnoreCase("Hud")) {
                newItems.add(new BindButton(setting));
            }
            if (setting.getValue() instanceof String || setting.getValue() instanceof Character) {
                newItems.add(new StringButton(setting));
            }
            if (setting.getValue() instanceof Image) {
                newItems.add(new ImageButton(setting));
            }
            if (setting.isNumberSetting()) {
                if (setting.hasRestriction()) {
                    newItems.add(new Slider(setting));
                    continue;
                }
                newItems.add(new UnlimitedSlider(setting));
            }
            if (setting.isEnumSetting()) {
                newItems.add(new EnumButton(setting));
            }
            if (setting.isColorSetting()) {
                newItems.add(new ColorPickerButton(setting));
            }
        }
        this.items = newItems;

    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!this.items.isEmpty()) {
            textManager.drawStringWithShadow(subOpen
                            ? ClickGui.getInstance().buttonOpen.getValue()
                            : ClickGui.getInstance().buttonClose.getValue(),
                    x - 1.5F + width - 7.4F, y - 1F - ForeverClientGui.getClickGui().getTextOffset(),
                    new Color(255, 255, 255, 255).getRGB());


            if (subOpen) {
                float height = 1F;
                animGui.update();
                for (Item item : this.items) {
                    if (!item.isHidden()) {
                        height += animGui.getCurrentFloat();
                        item.setLocation(x, y + height);
                        item.setHeight(12 * ForeverClientGui.getScale());
                        item.setWidth(width - 9);
                        item.drawScreen(mouseX, mouseY, partialTicks);
                        height += (item.getHeight() - 12F) * ForeverClientGui.getScale();
                    }
                    item.update();
                }
            }
            if (subOpen != subOpenOld) {
                updateVisibility();
                subOpenOld = subOpen;
            }
        }
        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {
            anim.update();
            String description = module.getDescription();
            flag = true;
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

                GuiRenderUtil.drawBorderedRect(mouseX + 6, mouseY - 2, mouseX + width + 9, mouseY + i, 1F, new Color(0, 0, 0, (int) anim.getCurrent()).getRGB(), new Color(0, 0, 0, 255).getRGB());
                i = 0;
                for (String des : descs) {
                    textManager.drawStringWithShadow(des, mouseX + 8, mouseY + i, Color.WHITE.getRGB());
                    i += textManager.getFontHeight() + 2;
                }

            });
        } else if(flag) {
            anim.reset();
            flag = false;
        }

    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                this.subOpen = !this.subOpen;
                ModuleButton.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            if (this.subOpen) {
                for (final Item item : this.items) {
                    if (!item.isHidden()) {
                        item.mouseClicked(mouseX, mouseY, mouseButton);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        super.mouseReleased(mouseX, mouseY, releaseButton);
        if (!this.items.isEmpty()) {
            for (final Item item : this.items) {
                if (!item.isHidden()) {
                    item.mouseReleased(mouseX, mouseY, releaseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(final char typedChar, final int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && this.subOpen) {
            for (final Item item : this.items) {
                if (!item.isHidden()) {
                    item.onKeyTyped(typedChar, keyCode);
                }
            }
        }
    }

    @Override
    public void updateVisibility() {
        animGui.reset();
        if (!this.items.isEmpty()) {
            for (final Item item : this.items) {
                item.updateVisibility();

            }


        }
    }

    @Override
    public float getHeight() {
        if (this.subOpen) {
            int height = 11;
            for (final Item item : this.items) {
                if (!item.isHidden()) {
                    height += item.getHeight() + 1;
                }
            }
            return height * ForeverClientGui.getScale() + 2;
        }
        return 11 * ForeverClientGui.getScale();
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }

    @Override
    public boolean getState() {
        return this.module.isEnabled();
    }
}
