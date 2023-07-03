package we.devs.forever.client.ui.foreverClientGui.components.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.render.animation.TimeAnimation;
import we.devs.forever.api.util.test.FileChooser;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ImageButton extends Button {
    private final Setting<Image> setting;
    protected TimeAnimation anim;
    File file = null;
    FileChooser fileChooser = new FileChooser() {
        @Override
        protected JDialog createDialog(Component parent)
                throws HeadlessException {
            JDialog dialog = super.createDialog(parent);
            // config here as needed - just to see a difference
            dialog.setLocationByPlatform(true);
            // might help - can't know because I can't reproduce the problem
            dialog.setAlwaysOnTop(true);
            return dialog;
        }
    };
    public float loadWidth = textManager.getStringWidth("Load") + 5.5F;
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Images ", "png", "jpeg", "jpg", "gif");

    public ImageButton(Setting<Image> setting) {
        super(setting.getName());
        this.setting = setting;
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle("Image loader");
        Action details = fileChooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Downloads"));
        anim = new TimeAnimation(200, 0, 94.4 * ForeverClientGui.getScale(), false);

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(file != null) {
            setting.getValue().deleteImages();
            setting.setValue(new Image(file.getAbsolutePath()));
            file =null;
        }
        anim.update();
        RenderUtil.drawRect(x, y, x + anim.getCurrentFloat(), y + height - 0.5f, ClickGui.getInstance().mainColor.getColor().getRGB());

        GuiRenderUtil.drawBorderedRect(x + 1.5F, y + .5F,
                x + loadWidth,
                y + height - 1f, 1F,
                ColorUtil.getColorAlpha(ClickGui.getInstance().mainColor.getColor(), 127), Color.BLACK.getRGB());

        textManager.drawStringWithShadow("Load", x + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), 0xFFFFFFFF);

        textManager.drawStringWithShadow(setting.getValue().getImageFile() == null ? "None" : setting.getValue().getImageFile().getName(), x + loadWidth + 2.3F, y - 1.65F - ForeverClientGui.getClickGui().getTextOffset(), 0xFFFFFFFF);


        //isHovering(mouseX, mouseY) ? ColorUtil.getColorAlpha(ClickGui.getGui().mainColor.getColor(), ClickGui.getGui().hoverAlpha.getValue())


        if (ClickGui.getInstance().desc.getValue() && this.isHovering(mouseX, mouseY)) {
            String description =
                    ChatFormatting.GREEN +
                            "Value " +
                            ChatFormatting.WHITE +
                            (setting.getValue().getImageFile() == null ? "None" : setting.getValue().getImageFile().getAbsolutePath()) +
                            "." +
                            "\n" +
                            ChatFormatting.WHITE +
                            (setting.getDescription().equals("") ? "A Image setting." : setting.getDescription());
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
    public void update() {
        this.setHidden(!setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
//        if (isHovering(mouseX, mouseY)) {
//            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
//        }

        if (isHovering(mouseX, mouseY, x + 1.5F, y + .5F, x + loadWidth, y + height - 1)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            Thread thread = new Thread(() -> {
                fileChooser.showOpenDialog(null);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (fileChooser.getSelectedFile() == null) return;
                String path = fileChooser.getSelectedFile().getPath();
                if (path.endsWith(".png") ||
                        path.endsWith(".jpg") ||
                        path.endsWith(".jpeg") ||
                        path.endsWith(".gif")) {
                    file = fileChooser.getSelectedFile();
                } else {
                    Command.sendMessage(ChatFormatting.RED + "Wrong file");
                }
            });
            thread.start();


        }
    }

    @Override
    public void updateVisibility() {
        // anim= new TimeAnimation(250, 0,getWidth() + 7.4,false);
        anim.reset();
    }

    @Override
    public float getHeight() {
        return 12;
    }


}
