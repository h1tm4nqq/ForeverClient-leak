package we.devs.forever.client.modules.impl.render;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.render.Render2DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;

public
class Fullbright extends Module {

    public static Fullbright INSTANCE;
    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Gamma));
    public Setting<Boolean> effects = (new Setting<>("Effects", false));
    public Setting<Boolean> damageScreen = (new Setting<>("DamageScreen", false));
    public Setting<Color> damageScreenColor = (new Setting<>("DamageScreenColor", new Color(30, 30, 30, 30), ColorPickerButton.Mode.Normal, 100, v -> damageScreen.getValue()));

    private float previousSetting = 1f;
    int dynamic_alpha = 0;
    int nuyahz = 0;

    public Fullbright() {
        super("Fullbright", "Makes your game brighter.", Category.RENDER);
        INSTANCE = this;
    }

    public static Fullbright getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        previousSetting = mc.gameSettings.gammaSetting;
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.Gamma) {
            mc.gameSettings.gammaSetting = 1000f;
        }

        if (mode.getValue() == Mode.Potion) {
            mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
        }
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == Mode.Potion) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
        mc.gameSettings.gammaSetting = previousSetting;
    }

    @EventListener
    public void onRender2D(Render2DEvent e){
        if(damageScreen.getValue()) {
            Color color2 = new Color(damageScreenColor.getColor().getRed(),damageScreenColor.getColor().getGreen(),damageScreenColor.getColor().getBlue(), MathUtil.clamp(dynamic_alpha + 40,0,255));
            if(mc.player.getHealth() < 10) {
                ScaledResolution sr = new ScaledResolution(mc);
                draw2DGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledWidth(), color2.getRGB(), new Color(0,0,0,0).getRGB(),  color2.getRGB(), new Color(0,0,0,0).getRGB());
             //   RenderUtil.drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledWidth(), color2.getRGB(), new Color(0,0,0,0).getRGB(),  color2.getRGB(), new Color(0,0,0,0).getRGB());
                if(mc.player.getHealth() > 9){
                    nuyahz = 18;
                } else
                if(mc.player.getHealth() > 8){
                    nuyahz = 36;
                } else
                if(mc.player.getHealth() > 7){
                    nuyahz = 54;
                } else
                if(mc.player.getHealth() > 6){
                    nuyahz = 72;
                } else
                if(mc.player.getHealth() > 5){
                    nuyahz = 90;
                } else
                if(mc.player.getHealth() > 4){
                    nuyahz = 108;
                } else
                if(mc.player.getHealth() > 3){
                    nuyahz = 126;
                } else
                if(mc.player.getHealth() > 2){
                    nuyahz = 144;
                } else
                if(mc.player.getHealth() > 1){
                    nuyahz = 162;
                } else
                if(mc.player.getHealth() > 0){
                    nuyahz = 180;
                }
            }
            if(nuyahz > dynamic_alpha){
                dynamic_alpha = dynamic_alpha + 3;
            }
            if(nuyahz < dynamic_alpha){
                dynamic_alpha = dynamic_alpha - 3;
            }
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getStage() == 0 && event.getPacket() instanceof SPacketEntityEffect) {
            if (this.effects.getValue()) {
                SPacketEntityEffect packet = event.getPacket();
                if (mc.player != null && packet.getEntityId() == mc.player.getEntityId() && (packet.getEffectId() == 9 || packet.getEffectId() == 15)) {
                    event.cancel();
                }
            }
        }
    }
    public static void draw2DGradientRect(float left, float top, float right, float bottom, int leftBottomColor, int leftTopColor, int rightBottomColor, int rightTopColor) {
        float lba = (float) (leftBottomColor >> 24 & 255) / 255.0F;
        float lbr = (float) (leftBottomColor >> 16 & 255) / 255.0F;
        float lbg = (float) (leftBottomColor >> 8 & 255) / 255.0F;
        float lbb = (float) (leftBottomColor & 255) / 255.0F;
        float rba = (float) (rightBottomColor >> 24 & 255) / 255.0F;
        float rbr = (float) (rightBottomColor >> 16 & 255) / 255.0F;
        float rbg = (float) (rightBottomColor >> 8 & 255) / 255.0F;
        float rbb = (float) (rightBottomColor & 255) / 255.0F;
        float lta = (float) (leftTopColor >> 24 & 255) / 255.0F;
        float ltr = (float) (leftTopColor >> 16 & 255) / 255.0F;
        float ltg = (float) (leftTopColor >> 8 & 255) / 255.0F;
        float ltb = (float) (leftTopColor & 255) / 255.0F;
        float rta = (float) (rightTopColor >> 24 & 255) / 255.0F;
        float rtr = (float) (rightTopColor >> 16 & 255) / 255.0F;
        float rtg = (float) (rightTopColor >> 8 & 255) / 255.0F;
        float rtb = (float) (rightTopColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(rtr, rtg, rtb, rta).endVertex();
        bufferbuilder.pos(left, top, 0).color(ltr, ltg, ltb, lta).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(lbr, lbg, lbb, lba).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(rbr, rbg, rbb, rba).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    public
    enum Mode {
        Gamma,
        Potion
    }
}
