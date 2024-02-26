package we.devs.forever.client.modules.impl.client;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public class ClickGui extends Module {
    private static ClickGui INSTANCE;
    public Setting<Boolean> colorSync = (new Setting<>("Sync", true));
    public Setting<Boolean> rainbowRolling = (new Setting<>("RollingRainbowBreak", false, v -> {
        if (!colorSync.getValue()) return false;
        return false;
    }));
    public ForeverClientGui foreverClientGui;
    public Setting<Boolean> desc = (new Setting<>("Descriptions", false));
    public Setting<String> prefix = (new Setting<>("Prefix", "."));
    public Setting<Float> scale = (new Setting<>("Scale", 1.0F, 0.1F, 1.0F));
    public static Setting<Color> mainColor = (new Setting<>("Main color", new Color(21, 107, 183, 255), ColorPickerButton.Mode.Normal, 100));
    public static Setting<Color> bgGuiColor = (new Setting<>("Gui BG color", new Color(67, 67, 67, 110), ColorPickerButton.Mode.Normal, 100));
    public Setting<Integer> hoverAlpha = (new Setting<>("HoverAlpha", 240, 0, 255));
    public Setting<Float> olWidth = (new Setting<>("Outline Width", 1.0f, 0.1f, 5f));
    public Setting<String> buttonClose = (new Setting<>("ButtonsClose", "+"));
    public Setting<String> buttonOpen = (new Setting<>("ButtonsOpen", "-"));
    public static Setting<Boolean> images = (new Setting<>("Images", true));
    public Setting<Boolean> shader = (new Setting<>("Blur", true));
    public static Setting<Boolean> bg = (new Setting<>("Background", true));
    public static Setting<Color> bgColorTop = (new Setting<>("BGTopColor", new Color(60, 60, 61, 111), ColorPickerButton.Mode.Normal, 100, v -> bg.getValue()));
    public static Setting<Color> bgColorBottom = (new Setting<>("BGBottomColor", new Color(4, 129, 255, 124), ColorPickerButton.Mode.Normal, 100, v -> bg.getValue()));
    public Setting<Particle> particles = (new Setting<>("Particles", Particle.None));
    public Setting<Integer> particleLength = (new Setting<>("Particle Length", 203, 0, 300, v -> particles.getValue() != Particle.None));
    public Setting<Float> particleSize = (new Setting<>("Particle Size", 4.0F, 0.1F, 25.0F, v -> particles.getValue() != Particle.None));
    public Setting<Color> particleColor = (new Setting<>("ParticleColor", new Color(21, 107, 183, 255), ColorPickerButton.Mode.Normal, 100, v -> particles.getValue() != Particle.None));
    public Setting<Color> lineColor = (new Setting<>("LineColor", new Color(21, 107, 183, 255), ColorPickerButton.Mode.Normal, 100, v -> particles.getValue() != Particle.None));
    public Setting<Anime> anime = (new Setting<>("AnimeMode", Anime.None));
    public Setting<String> path = (new Setting<>("Path", "", v -> anime.getValue() == Anime.Custom));
    public Setting<Integer> posX = (new Setting<>("PosX", 100, 0, 2000, v -> anime.getValue() != Anime.None));
    public Setting<Integer> posY = (new Setting<>("PosY", 100, 0, 2000, v -> anime.getValue() != Anime.None));
    public Setting<Integer> fadeintimeout = (new Setting<>("FadeInTimeOut", 512, 0, 2048, v -> anime.getValue() != Anime.None));
    public Setting<Float> fadeintimespeed = (new Setting<>("FadeInTimeSpeed", 0.5F, 0.0F, 5.0F, v -> anime.getValue() != Anime.None));
    public Setting<Integer> scaleX = (new Setting<>("ScaleX", 405, 0, 1024, v -> anime.getValue() != Anime.None));
    public Setting<Integer> scaleY = (new Setting<>("ScaleY", 405, 0, 1024, v -> anime.getValue() != Anime.None));
    private float scaleO;

    public ClickGui() {
        super("ClickGui", "Opens client interface", Category.CLIENT, false,true, Keyboard.KEY_RSHIFT, false, KeyMode.Release,true);
        scaleO = scale.getValue();
        setInstance();
    }

    public static ClickGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGui();
        }
        return INSTANCE;
    }


    private void setInstance() {
        INSTANCE = this;
    }


    public Color getModuleColor() {
        return mainColor.getColor();
    }

    public Color getBgColor() {
        return bgGuiColor.getColor();
    }

    @Override
    public void onEnable() {
    if (scaleO == scale.getValue()) {
            mc.displayGuiScreen(foreverClientGui);
        } else {
            scaleO = scale.getValue();
            textManager.setFontRenderer("/assets/forever/fonts/RobotoFlex.ttf",0, (int) (17 *scaleO));
            foreverClientGui = ForeverClientGui.getClickGui();
            mc.displayGuiScreen(foreverClientGui);
        }

        if (shader.getValue()) {
            if (OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer) {
                //mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
            }
        }
        if (!shader.getValue()) {
            if (mc.entityRenderer.getShaderGroup() != null) {
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
                mc.entityRenderer.stopUseShader();
            }
        }
    }



    public float getOutlineWidth() {
        return olWidth.getValue();
    }


    @EventListener
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2) {
            if (event.getSetting().getFeature().equals(this)) {
                if (event.getSetting().equals(prefix)) {
                    commandManager.setPrefix(prefix.getValue());
                    Command.sendMessage("Prefix set to " + TextUtil.BLUE + commandManager.getPrefix());
                }
            }
        }
    }

    @Override
    public void onLoad() {
        foreverClientGui = ForeverClientGui.getClickGui();
        commandManager.setPrefix(prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(mc.currentScreen instanceof ForeverClientGui)) {
            disable();
        }
//        if (reload.getValue()) {
//            gif = new Image(path.getValue());
//            reload.setValue(false);
//        }

    }

    @Override
    public void onDisable() {
        mc.entityRenderer.stopUseShader();
        if (mc.currentScreen instanceof ForeverClientGui) {
            mc.displayGuiScreen(null);
        }
    }



    public enum Anime {
        None,
        You,
        AnimeBunny,
        AnimeVSS,
        AnimeKalash,
        AnimeNeko,
        Custom
    }

    public enum Particle {
        None,
        New,
        Snowing
    }
}
