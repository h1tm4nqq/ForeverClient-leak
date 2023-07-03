package we.devs.forever.client.ui.foreverClientGui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.api.util.shaders.impl.fill.RainbowShader;
import we.devs.forever.api.util.shaders.impl.outline.RainbowOutLineShader;
import we.devs.forever.api.util.test.LoggerException;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.client.ClickGui;
import we.devs.forever.client.modules.impl.client.HudEditor;
import we.devs.forever.client.ui.Fonts.CustomFont;
import we.devs.forever.client.ui.foreverClientGui.components.Component;
import we.devs.forever.client.ui.foreverClientGui.components.items.Item;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ModuleButton;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;
import we.devs.forever.client.ui.foreverClientGui.particle.ParticleSystem;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class ForeverClientGui extends GuiScreen {

    private static ForeverClientGui INSTANCE;
    private final ArrayList<Component> components = new ArrayList<>();
    public static CustomFont fontButtons;
    public static  CustomFont fontComponent;

    static boolean shouldRender;
    public ParticleSystem particleSystem;
    TimerUtil timer = new TimerUtil();
    float fadeinnn;
    private Component hudGui;
    static float scale = ClickGui.getInstance().scale.getValue();
    static Runnable runnable;

    public static float getScale() {
        return scale;
    }

    public ForeverClientGui() {
        scale = ClickGui.getInstance().scale.getValue();
        setInstance();
        LoggerException.testMethod(this::load);
    }

    public static ForeverClientGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForeverClientGui();
        }
        return INSTANCE;
    }

    public static ForeverClientGui getClickGui() {
        return getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static void setDesc(Runnable run) {
        runnable = run;
        shouldRender = true;
    }

    private void load() {
//        fontButtons = new CustomFont(new Font(/*"/assets/forever/fonts/Montserrat-Light.ttf"*/"Verdana", Font.PLAIN, (int) (17 * ClickGui.getInstance().scale.getValue())), true, true);
//        fontComponent = new CustomFont(new Font(/*"/assets/forever/fonts/Montserrat-Light.ttf"*/"Verdana", Font.PLAIN, (int) (18 * ClickGui.getInstance().scale.getValue())), false, true);
        int x = (int) (-103 * getScale());
        for (Module.Category category :Client.moduleManager.getCategories()) {
            if (category == Module.Category.HUD) {
                hudGui = new Component(category.getName(), category.getImage(), 100, 100, true) {
                    @Override
                    public void setupItems() {
                        moduleManager.getHudModules().forEach(module -> {
                            addButton(new ModuleButton(module));
                        });
                    }
                };
                hudGui.getItems().sort(Comparator.comparing(Client::getName));
            } else {
                components.add(new Component(category.getName(), category.getImage(), x += 105 * getScale(), 2, true) {
                    @Override
                    public void setupItems() {
                        moduleManager.getModulesByCategory(category).forEach(module -> {
                            addButton(new ModuleButton(module));
                        });
                    }
                });
                components.forEach(components -> components.getItems().sort(Comparator.comparing(Client::getName)));
            }
        }
    }

    public void updateModule(Module module) {
        for (Component component : this.components) {
            for (Item item : component.getItems()) {
                if (item instanceof ModuleButton) {
                    ModuleButton button = (ModuleButton) item;
                    Module mod = button.getModule();
                    if (module != null && module.equals(mod)) {
                        button.initSettings();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawScreen0(mouseX,mouseY ,partialTicks);




    }

    public void drawScreen0(int mouseX, int mouseY, float partialTicks) {
        
        shouldRender = false;


        if (ClickGui.getInstance().bg.getValue() && !HudEditor.INSTANCE.isEnabled()) {
            this.drawGradientRect(0, 0, Display.getWidth(), Display.getHeight(), ClickGui.getInstance().bgColorTop.getValue().getRGB(), ClickGui.getInstance().bgColorBottom.getValue().getRGB());
        }
        if (ClickGui.getInstance().particles.getValue() == ClickGui.Particle.New && !HudEditor.INSTANCE.isEnabled()) {
            final ScaledResolution res = new ScaledResolution(mc);

            if (this.particleSystem != null) {
                particleSystem.drawScreen(mouseX, mouseY, partialTicks);
            } else {
                this.particleSystem = new ParticleSystem();
            }
        }
        if (ClickGui.getInstance().anime.getValue() != ClickGui.Anime.None && !HudEditor.INSTANCE.isEnabled()) {
            final float xOffset = ClickGui.getInstance().posX.getValue() + 10.0f;
            final float yOffset = ClickGui.getInstance().posY.getValue();
            final float mouseposx = mouseX / 100.0f;
            final float mouseposy = mouseY / 100.0f;
            float fadeinn = timer.getPassedTimeMs() / ClickGui.getInstance().fadeintimespeed.getValue();
            if (fadeinn < ClickGui.getInstance().fadeintimeout.getValue()) {
                fadeinnn = fadeinn;
            }

            if (ClickGui.getInstance().anime.getValue() == ClickGui.Anime.You) {
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/anime1.png"));
                RenderUtil.drawCompleteImage(xOffset - 1.0f + mouseposx - fadeinnn, yOffset - 1.0f - mouseposy, ClickGui.getInstance().scaleX.getValue(), ClickGui.getInstance().scaleY.getValue());
            }
            if (ClickGui.getInstance().anime.getValue() == ClickGui.Anime.AnimeBunny) {
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/anime2.png"));
                RenderUtil.drawCompleteImage(xOffset - 1.0f + mouseposx - fadeinnn, yOffset - 1.0f - mouseposy, ClickGui.getInstance().scaleX.getValue(), ClickGui.getInstance().scaleY.getValue());
            }
            if (ClickGui.getInstance().anime.getValue() == ClickGui.Anime.AnimeVSS) {
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/anime3.png"));
                RenderUtil.drawCompleteImage(xOffset - 1.0f + mouseposx - fadeinnn, yOffset - 1.0f - mouseposy, ClickGui.getInstance().scaleX.getValue(), ClickGui.getInstance().scaleY.getValue());
            }
            if (ClickGui.getInstance().anime.getValue() == ClickGui.Anime.AnimeKalash) {
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/anime4.png"));
                RenderUtil.drawCompleteImage(xOffset - 1.0f + mouseposx - fadeinnn, yOffset - 1.0f - mouseposy, ClickGui.getInstance().scaleX.getValue(), ClickGui.getInstance().scaleY.getValue());
            }
            if (ClickGui.getInstance().anime.getValue() == ClickGui.Anime.AnimeNeko) {
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/anime5.png"));
                RenderUtil.drawCompleteImage(xOffset - 1.0f + mouseposx - fadeinnn, yOffset - 1.0f - mouseposy, ClickGui.getInstance().scaleX.getValue(), ClickGui.getInstance().scaleY.getValue());
            }
//            if (ClickGui.getInstance().anime.getValue() == ClickGui.Anime.CUSTOM) {
//                mc.getTextureManager().bindTexture(ClickGui.getInstance().gif.getImage());
//                RenderUtil.drawCompleteImage(xOffset - 1.0f + mouseposx - fadeinnn, yOffset - 1.0f - mouseposy, ClickGui.getInstance().scaleX.getValue(), ClickGui.getInstance().scaleY.getValue());
//            }
        }
        if (HudEditor.INSTANCE.isEnabled()) {
            int y = mc.displayHeight / 4;
            int x = mc.displayWidth / 4;
//            ForeverClient.LOGGER.info("X:" + x
//                    +" Y:" + y
//                    + "\nX:" + mc.displayWidth
//                    +" Y:" + mc.displayHeight);
            RenderUtil.drawLine(0, y, mc.displayWidth, y, 2, ColorUtil.toRGBA(214, 192, 203, 200));
            RenderUtil.drawLine(x, 0, x, mc.displayHeight, 2, ColorUtil.toRGBA(214, 192, 203, 200));
        }


        checkMouseWheel();
        execute(components -> components.drawScreen(mouseX, mouseY, partialTicks));
        executeHud(h -> h.mouseUpdated(mouseX, mouseY));


        if (shouldRender) {
            runnable.run();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        execute(components -> components.mouseClicked(mouseX, mouseY, clickedButton));
        executeHud(h -> h.mouseClicked(mouseX, mouseY, clickedButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        execute(components -> components.mouseReleased(mouseX, mouseY, releaseButton));
        executeHud(h -> h.mouseReleased(mouseX, mouseY, releaseButton));
    }


    public final ArrayList<Component> getComponents() {
        return components;
    }

    public void checkMouseWheel() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            execute(component -> component.setY(component.getY() - 15));
        } else if (dWheel > 0) {
            execute(component -> component.setY(component.getY() + 15));
        }
    }

    public float getTextOffset() {
        return -4.5F;
    }


    public Component getComponentByName(String name) {
        for (Component component : this.components) {
            if (component.getName().equalsIgnoreCase(name)) {
                return component;
            }
        }
        return null;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (HudEditor.INSTANCE.isEnabled())
                HudEditor.INSTANCE.disable();
            else
                mc.displayGuiScreen(null);
            return;
        }
        components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }

    @Override
    public void updateScreen() {
        if (this.particleSystem != null) {
            this.particleSystem.update();
        }
    }

    private void execute(Consumer<? super Component> action) {
        if (HudEditor.INSTANCE.isEnabled())
            action.accept(hudGui);
        else
            components.forEach(action);
    }

    private void executeHud(Consumer<? super Hud> action) {
        if (HudEditor.INSTANCE.isEnabled()) {
            Client.moduleManager.getHudModules().forEach(action);
        }
    }
}