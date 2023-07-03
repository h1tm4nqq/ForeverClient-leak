package we.devs.forever.client.ui.newGui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.util.render.GuiRenderUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.modules.impl.client.Colors;
import we.devs.forever.client.modules.impl.client.Config1;
import we.devs.forever.client.ui.Fonts.font.FontRenderer;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.Component;
import we.devs.forever.client.ui.foreverClientGui.particle.ParticleSystem;
import we.devs.forever.client.ui.newGui.api.Frame;
import we.devs.forever.client.ui.newGui.impl.Panel;
import we.devs.forever.client.ui.newGui.impl.frames.ModuleFrame;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class NewGui extends GuiScreen {
    public static FontRenderer fontButtons, fontComponent,fontBeta;
    public static ArrayList<Panel> panels = new ArrayList<>();
    public final float scale;
    private float x1;
    private float x = 400F, y = 1F, width = 200F, height = 15F;
    private final float clickGui, hudEditor, windows, console;
    private final ParticleSystem particleSystem = new ParticleSystem();
    private GuiMode guiMode = GuiMode.ClickGui;

    public NewGui() {
        panels.clear();
        fontButtons = new FontRenderer(new Font("/assets/forever/fonts/RobotoFlex.ttf", Font.PLAIN, (int) (17 * ClickGui.getInstance().scale.getValue())));
        fontComponent = new FontRenderer(new Font("/assets/forever/fonts/RobotoFlex.ttf",Font.PLAIN, (int) (18 * ClickGui.getInstance().scale.getValue())));
        fontBeta = new FontRenderer( new Font("/assets/forever/fonts/RobotoFlex.ttf",Font.PLAIN, (int) (13 * ClickGui.getInstance().scale.getValue())));
        Frame.scale = ClickGui.getInstance().scale.getValue();
        scale = Frame.scale;
        x1 = 40 * Frame.scale;
        Client.moduleManager.getCategories().stream().filter(category -> category != Module.Category.HUD).forEach(category -> {
            ArrayList<ModuleFrame> frames = Client.moduleManager.getModulesByCategory(category).stream()
                    .sorted(Comparator.comparing(Client::getName))
                    .map(ModuleFrame::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            Panel panel = new Panel(category, frames, x1, 35);
            panels.add(panel);
            x1 += 106 * Frame.scale;
        });
        clickGui = fontComponent.getStringWidth("ClickGui");
        hudEditor = fontComponent.getStringWidth("HudEditor");
        windows = fontComponent.getStringWidth("Windows");
        console = fontComponent.getStringWidth("Console");
    }

    public void checkMouseWheel() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            panels.forEach(panel -> panel.setY(panel.getY() - 15));
        } else if (dWheel > 0) {
            panels.forEach(panel -> panel.setY(panel.getY() + 15));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        if (ClickGui.getInstance().particles.getValue().equals(ClickGui.Particle.New))
            particleSystem.drawScreen(mouseX, mouseY, partialTicks);
        GuiRenderUtil.drawRoundedRectangle(x, y, width, height, 7, Config1.color.getColor());
        fontComponent.drawStringWithShadow("ClickGui", x + 8F * scale,
                (isHovering(mouseX, mouseY, x + 8F * scale, y + 4.5F * scale, clickGui, fontComponent.getHeight()) ? 3.5F : 4.5F + y) * scale,
                guiMode == GuiMode.ClickGui ? Color.RED.getRGB() : Color.WHITE.getRGB());

        fontComponent.drawStringWithShadow("HudEditor", x + 54F * scale,
                (isHovering(mouseX, mouseY, x + 54F * scale, y + 4.5F * scale, hudEditor, fontComponent.getHeight()) ? 3.5F : 4.5F + y) * scale,
                guiMode == GuiMode.HudEditor ? Color.RED.getRGB() : Color.WHITE.getRGB());

        fontComponent.drawStringWithShadow("Windows", x + 108F * scale,
                (isHovering(mouseX, mouseY, x + 108F * scale, y + 4.5F * scale, windows, fontComponent.getHeight()) ? 3.5F : 4.5F + y) * scale,
                guiMode == GuiMode.Windows ? Color.RED.getRGB() : Color.WHITE.getRGB());

        fontComponent.drawStringWithShadow("Console", x + 157F * scale,
                (isHovering(mouseX, mouseY, x + 157F * scale, y + 4.5F * scale, console, fontComponent.getHeight()) ? 3.5F : 4.5F + y) * scale,
                guiMode == GuiMode.Console ? Color.RED.getRGB() : Color.WHITE.getRGB());
        checkMouseWheel();
        if (guiMode == GuiMode.ClickGui) panels.forEach(panel -> panel.drawScreen(mouseX, mouseY, partialTicks));
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        panels.forEach(panel -> panel.mouseClicked(mouseX, mouseY, mouseButton));
        if (mouseButton == 0) {
            if (isHovering(mouseX, mouseY, x + 8F * scale, y + 4.5F * scale, clickGui, fontComponent.getHeight())) {
                guiMode = GuiMode.ClickGui;
            }
            if (isHovering(mouseX, mouseY, x + 54F * scale, y + 4.5F * scale, hudEditor, fontComponent.getHeight())) {
                guiMode = GuiMode.HudEditor;
            }
            if (isHovering(mouseX, mouseY, x + 108F * scale, y + 4.5F * scale, windows, fontComponent.getHeight())) {
                guiMode = GuiMode.Windows;
            }
            if (isHovering(mouseX, mouseY, x + 157F * scale, y + 4.5F * scale, console, fontComponent.getHeight())) {
                guiMode = GuiMode.Console;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (guiMode == GuiMode.ClickGui) panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void updateScreen() {
        if (guiMode == GuiMode.ClickGui) panels.forEach(Panel::updateScreen);
        particleSystem.update();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            Colors.getInstance().disable();
            return;
        }
        if (guiMode == GuiMode.ClickGui) panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
    }

    protected boolean isHovering(int mouseX, int mouseY, float x, float y, float width, float height) {
        for (Component component : ForeverClientGui.getClickGui().getComponents()) {
            if (component.drag) {
                return false;
            }
        }
        return x < mouseX && width + x > mouseX && y < mouseY && height + y > mouseY;
    }




    public enum GuiMode {
        ClickGui,
        HudEditor,
        Windows,
        Console;
    }
}
