
/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiMultiplayer
 *  net.minecraft.client.gui.GuiOptions
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.GuiWorldSelection
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.util.ResourceLocation
 *  org.lwjgl.opengl.GL11
 */
package we.devs.forever.client.ui.customScreen;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.ui.alts.ias.gui.GuiAccountSelector;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GuiCustomMainScreen extends GuiScreen {
    private final ResourceLocation resourceLocation = new ResourceLocation("textures/mainscreen.png");
    private int y;
    private int x;


    public static boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height;
    }

    public void initGui() {
        this.x = this.width / 2;
        this.y = this.height / 4 + 48;
        this.buttonList.add(new TextButton(0, this.x, this.y + 20, "Singleplayer"));
        this.buttonList.add(new TextButton(1, this.x, this.y + 44, "Multiplayer"));
        this.buttonList.add(new TextButton(1, this.x, this.y + 66, "ForeverClient"));
        this.buttonList.add(new TextButton(2, this.x, this.y + 88, "Settings"));
        this.buttonList.add(new TextButton(2, this.x, this.y +110, "Alts"));
        this.buttonList.add(new TextButton(2, this.x, this.y + 132, "Exit"));
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(7425);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

//    public void updateScreen() {
//        super.updateScreen();
//    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (GuiCustomMainScreen.isHovered(this.x - Client.textManager.getStringWidth("Singleplayer") / 2, this.y + 20, Client.textManager.getStringWidth("Singleplayer"), Client.textManager.getFontHeight(), mouseX, mouseY)) {
            this.mc.displayGuiScreen(new GuiWorldSelection(this));
        } else if (GuiCustomMainScreen.isHovered(this.x - Client.textManager.getStringWidth("Multiplayer") / 2, this.y + 44, Client.textManager.getStringWidth("Multiplayer"), Client.textManager.getFontHeight(), mouseX, mouseY)) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        } else if (GuiCustomMainScreen.isHovered(this.x - Client.textManager.getStringWidth("ForeverClient") / 2, this.y + 66, Client.textManager.getStringWidth("ForeverClient"), Client.textManager.getFontHeight(), mouseX, mouseY)) {
            this.mc.displayGuiScreen(ClickGui.getInstance().foreverClientGui);
            ClickGui.getInstance().setEnabled(true);
        } else if (GuiCustomMainScreen.isHovered(this.x - Client.textManager.getStringWidth("Settings") / 2, this.y + 88, Client.textManager.getStringWidth("Settings"), Client.textManager.getFontHeight(), mouseX, mouseY)) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
              } else if (GuiCustomMainScreen.isHovered(this.x - Client.textManager.getStringWidth("Alts") / 2, this.y + 110, Client.textManager.getStringWidth("Alts"), Client.textManager.getFontHeight(), mouseX, mouseY)) {
                   this.mc.displayGuiScreen(new GuiAccountSelector());
        } else if (GuiCustomMainScreen.isHovered(this.x - Client.textManager.getStringWidth("Exit") / 2, this.y + 132, Client.textManager.getStringWidth("Exit"), Client.textManager.getFontHeight(), mouseX, mouseY)) {
            this.mc.shutdown();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float xOffset = -1.0f * (((float) mouseX - (float) this.width / 2.0f) / ((float) this.width / 32.0f));
        float yOffset = -1.0f * (((float) mouseY - (float) this.height / 2.0f) / ((float) this.height / 18.0f));
        this.x = this.width / 2;
        this.y = this.height / 4 + 48;
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        this.mc.getTextureManager().bindTexture(this.resourceLocation);
        RenderUtil.drawCompleteImage(-16.0f + xOffset, -9.0f + yOffset, this.width + 32, this.height + 18);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public BufferedImage parseBackground(BufferedImage background) {
        int height;
        int width = 1920;
        int srcWidth = background.getWidth();
        int srcHeight = background.getHeight();
        for (height = 1080; width < srcWidth || height < srcHeight; width *= 2, height *= 2) {
        }
        BufferedImage imgNew = new BufferedImage(width, height, 2);
        Graphics g = imgNew.getGraphics();
        g.drawImage(background, 0, 0, null);
        g.dispose();
        return imgNew;
    }

    private static class TextButton
            extends GuiButton {
        public TextButton(int buttonId, int x, int y, String buttonText) {
            super(buttonId, x, y, Client.textManager.getStringWidth(buttonText), Client.textManager.getFontHeight(), buttonText);
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.enabled = true;
                this.hovered = (float) mouseX >= (float) this.x - (float) Client.textManager.getStringWidth(this.displayString) / 2.0f && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
                Client.textManager.drawStringWithShadow(this.displayString, (float) this.x - (float) Client.textManager.getStringWidth(this.displayString) / 2.0f, this.y, Color.WHITE.getRGB());
                if (this.hovered) {
                    RenderUtil.drawLine((float) (this.x - 1) - (float) Client.textManager.getStringWidth(this.displayString) / 2.0f, this.y + 2 + Client.textManager.getFontHeight(), (float) this.x + (float) Client.textManager.getStringWidth(this.displayString) / 2.0f + 1.0f, this.y + 2 + Client.textManager.getFontHeight(), 1.0f, Color.WHITE.getRGB());
                }
            }
        }

        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            return this.enabled && this.visible && (float) mouseX >= (float) this.x - (float) Client.textManager.getStringWidth(this.displayString) / 2.0f && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        }
    }
}

