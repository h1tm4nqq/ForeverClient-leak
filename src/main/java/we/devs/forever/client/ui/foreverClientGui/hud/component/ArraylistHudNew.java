package we.devs.forever.client.ui.foreverClientGui.hud.component;


import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ArraylistHudNew extends Hud{

    private Setting<Boolean> lines = new Setting<>("Lines", true);
    private Setting<Boolean> cute = new Setting<>("Cute", false);
    private Setting<Color> color = new Setting<>("LineColor", new Color(255, 85, 255, 255), ColorPickerButton.Mode.Normal,100);
    private Setting<Boolean> pulse = new Setting<>("Pulse", true);
    private Setting<Float> range = new Setting<>("Range", 1f, 0.1f, 1f);
    private Setting<Float> spread = new Setting<>("Spread", 1f, 0.1f, 2f);
    private Setting<Float> speed = new Setting<>("Speed", 1f, 1f, 10f);

    public ArraylistHudNew() {
        super("ArrayListNew");
    }

    float maxWidth = 0;


    @Override
    protected void onRenderHud() {


        final int[] yDist = {0};
        final int[] counter = {1};

        boolean isTop = anchor == Anchor.TOP_LEFT || anchor == Anchor.TOP_RIGHT;
        boolean isRight = anchor == Anchor.BOTTOM_RIGHT || anchor == Anchor.TOP_RIGHT;

        ArrayList<Module> modules = moduleManager.getEnabledModules()
                .stream()
                .filter(Module::getVisible)
                .collect(Collectors.toCollection(ArrayList::new));

        maxWidth = (float) modules
                .stream()
                .mapToDouble(m -> renderer.getStringWidth(m.getDisplayInfo() != null ? m.getName() + " " + m.getDisplayInfo() : m.getName()))
                .max().orElse(0);

        width = maxWidth;
        modules.stream()
                .sorted(Comparator.comparingInt(module -> isTop ? -(int) renderer.getStringWidth(module.getDisplayInfo() != null ? module.getName() + " " + module.getDisplayInfo() : module.getName()) : (int) renderer.getStringWidth(module.getDisplayInfo() != null ? module.getName() + " " + module.getDisplayInfo() : module.getName())))
                .forEach(module -> {
                    float stringWidth = renderer.getStringWidth(module.getDisplayInfo() != null ? module.getName() + " " + module.getDisplayInfo() : module.getName());
                    String moduleDisplay = getModuleDisplay(module);

                    // Background
                    drawRect(X.getValue() + (isRight ? width - stringWidth - 2 : 0), Y.getValue() + yDist[0], stringWidth + 2, (int) (renderer.getHeight() + 1.5F), new Color(20, 20, 20, 60).hashCode());


                    int color = getColor(counter[0]);

                    // Add optional eye candy line
                    if (lines.getValue()) {
                        drawRect(X.getValue() + (isRight ? width - stringWidth - 2 : stringWidth + 2), Y.getValue() + yDist[0], 1F, (int) (renderer.getHeight() + 1.5F), color);
                    }

                    renderer.drawStringWithShadow(moduleDisplay,   X.getValue().intValue() +(int) (isRight ? width - stringWidth : 0), (int) (Y.getValue() + yDist[0] + 0.5F), module.getVisible() ? color : Color.GRAY.getRGB());
                    yDist[0] += (int) (renderer.getHeight() + 1.5F);
                    counter[0]++;
                });

        height = yDist[0];
    }

    private int getColor(int index) {
        float[] hsb = Color.RGBtoHSB(color.getColor().getRed(),color.getColor().getGreen(), color.getColor().getBlue(),null);
        if (cute.getValue()) {
            return getCuteColor(index - 1);
        } else if (pulse.getValue()) {
            if (this.color.getRainbow()) {
                return rainbow(300 * index, hsb);
            } else {
                return pulse(index, hsb, this.spread.getValue(), this.speed.getValue(), range.getValue());
            }
        } else {
            return color.getColor().getRGB();
        }
    }

    private int getCuteColor(int index) {

        int size = moduleManager.getEnabledModules().size();

        int light_blue = new Color(91, 206, 250).getRGB();
        int white = Color.WHITE.getRGB();
        int pink = new Color(245, 169, 184).getRGB();

        int chunkSize = size / 5;

        if (index < chunkSize) {
            return light_blue;
        } else if (index < chunkSize * 2) {
            return pink;
        } else if (index < chunkSize * 3) {
            return white;
        } else if (index < chunkSize * 4) {
            return pink;
        } else if (index < chunkSize * 5) {
            return light_blue;
        }

        return light_blue;
    }



    private String getModuleDisplay(Module module) {
        if (module.getDisplayInfo() != null) {
            return module.getName() + TextUtil.SECTIONSIGN + "7 " + module.getDisplayInfo();
        } else {
            return module.getName();
        }
    }

    public static int rainbow(int delay, float[] hsb) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
    }

    public static int pulse(int delay, float[] hsb, float spread, float speed, float range) {
        double sin = Math.sin(spread * ((System.currentTimeMillis() / Math.pow(10, 2)) * (speed / 10) + delay));
        sin *= range;
        return Color.getHSBColor(hsb[0], hsb[1], (float) ((sin + 1) / 2) + ((1F -range) * 0.5F)).getRGB();
    }
    public static void drawRect(float x, float y, float w, float h, int color) {
        float right = x + w;
        float bottom = y + h;

        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.color(red, green, blue, alpha);

        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(x, bottom, 0.0D).endVertex(); // top left
        bufferBuilder.pos(right, bottom, 0.0D).endVertex(); // top right
        bufferBuilder.pos(right, y, 0.0D).endVertex(); // bottom right
        bufferBuilder.pos(x, y, 0.0D).endVertex(); // bottom left
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

}
