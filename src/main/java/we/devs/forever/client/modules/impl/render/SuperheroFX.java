package we.devs.forever.client.modules.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.Fonts.CustomFont;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SuperheroFX extends Module {
    public SuperheroFX() {
        super("SuperheroFX", "Draws sexy hiteffects", Category.RENDER);
    }




    public Setting<Double> delay = (new Setting<>("Delay", 1.0D, 0.0D, 10.0D));
    public Setting<Float> scale = (new Setting<>("Scale", 1.5f, 0.0f, 5.0f));
    public Setting<Integer> test = (new Setting<>("Test", 15, 0, 40));
    public Setting<Integer> extra = (new Setting<>("Extra", 1, 0, 5));
    public Setting<Boolean> randomColor = (new Setting<>("RandomColor", true));
    public Setting<Color> colourSetting = (new Setting<>("Color", new Color(50, 120, 230, 255), ColorPickerButton.Mode.Rainbow, 100));
    private final List<PopupText> popTexts = new CopyOnWriteArrayList<>();
    private final Random rand = new Random();
    private final TimerUtil timer = new TimerUtil();
    private static final String[] superHeroTextsBlowup = new String[]{"KABOOM", "BOOM", "POW", "KAPOW", "KABLEM"};
    private static final String[] superHeroTextsDamageTaken = new String[]{"OUCH", "ZAP", "BAM", "WOW", "POW", "SLAP"};

    CustomFont font = new CustomFont(new Font("/assets/forever/fonts/badaboom.ttf", Font.PLAIN, 19), true, false);
    @Override
    public void onUpdate() {
        this.popTexts.removeIf(PopupText::isMarked);
        this.popTexts.forEach(PopupText::Update);
    }



    @Override
    public void onRender3D(Render3DEvent event) {

        this.popTexts.forEach(pop -> {
            GlStateManager.pushMatrix();
            RenderUtil.glBillboardDistanceScaled((float) pop.pos.x, (float) pop.pos.y, (float) pop.pos.z, mc.player, scale.getValue());
            GlStateManager.disableDepth();
            GlStateManager.translate(-(font.getStringWidth(pop.getDisplayName()) / 2.0), 0.0, 0.0);
            font.drawStringWithShadow(pop.getDisplayName(), 0, 0, pop.color);

            GlStateManager.enableDepth();

            GlStateManager.popMatrix();
        });
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        try {
            if (event.getPacket() instanceof SPacketExplosion) {
                SPacketExplosion packet = event.getPacket();
                if (mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) < 20.0 && this.timer.passedMs((long) (this.delay.getValue() * 1000.0f))) {
                    this.timer.reset();
                    int len = rand.nextInt(extra.getValue());
                    for (int i = 0; i <= len; i++) {
                        Vec3d pos = new Vec3d(packet.getX() + rand.nextInt(4) - 2, packet.getY() + rand.nextInt(2), packet.getZ() + rand.nextInt(4) - 2);
                        PopupText popupText = new PopupText(ChatFormatting.ITALIC + SuperheroFX.superHeroTextsBlowup[this.rand.nextInt(SuperheroFX.superHeroTextsBlowup.length)], pos);
                        popTexts.add(popupText);
                    }
                }
            } else if (event.getPacket() instanceof SPacketEntityStatus) {
                SPacketEntityStatus packet = event.getPacket();
                Entity e = packet.getEntity((World) mc.world);
                if (packet.getOpCode() == 35) {
                    if (mc.player.getDistance(e) < 20.0f) {
                        PopupText popupText = new PopupText(ChatFormatting.ITALIC + "POP", e.getPositionVector().add((0), 1.0, (0)));
                        popTexts.add(popupText);
                    }
                } else if (packet.getOpCode() == 2) {
                    if (mc.player.getDistance(e) < 20.0f & e != mc.player) {
                        if (this.timer.passedMs((long) (this.delay.getValue() * 1000.0f))) {
                            this.timer.reset();
                            int len = rand.nextInt((int) Math.ceil(extra.getValue() / 2.0));
                            for (int i = 0; i <= len; i++) {
                                Vec3d pos = new Vec3d(e.posX + rand.nextInt(2) - 1, e.posY + rand.nextInt(2) - 1, e.posZ + rand.nextInt(2) - 1);
                                PopupText popupText = new PopupText(ChatFormatting.ITALIC + SuperheroFX.superHeroTextsDamageTaken[this.rand.nextInt(SuperheroFX.superHeroTextsBlowup.length)], pos);
                                popTexts.add(popupText);
                            }
                        }
                    }

                }
            } else if (event.getPacket() instanceof SPacketDestroyEntities) {
                SPacketDestroyEntities packet = event.getPacket();
                final int[] array = packet.getEntityIDs();
                for (int i = 0; i < array.length - 1; i++) {
                    int id = array[i];
                    try {
                        //wtf is this?
                        if (mc.world.getEntityByID(id) == null) continue;
                    } catch (ConcurrentModificationException exception) {
                        return;
                    }
                    Entity e = mc.world.getEntityByID(id);
                    if (e != null && e.isDead) {
                        if ((mc.player.getDistance(e) < 20.0f & e != mc.player) && e instanceof EntityPlayer) {
                            for (int t = 0; t <= rand.nextInt(extra.getValue()); t++) {
                                Vec3d pos = new Vec3d(e.posX + rand.nextInt(2) - 1, e.posY + rand.nextInt(2) - 1, e.posZ + rand.nextInt(2) - 1);
                                PopupText popupText = new PopupText(ChatFormatting.ITALIC + String.valueOf(ChatFormatting.BOLD) + "EZ", pos);
                                popTexts.add(popupText);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t){}

    }


    class PopupText {
        private String displayName;
        private Vec3d pos;
        private boolean markedToRemove;
        private int color;
        private TimerUtil timer;
        private double yIncrease;

        public PopupText(final String displayName, final Vec3d pos) {
            this.timer = new TimerUtil();
            this.yIncrease = Math.random();
            while (this.yIncrease > 0.025 || this.yIncrease < 0.011) {
                this.yIncrease = Math.random();
            }
            this.timer.reset();
            this.setDisplayName(displayName);
            this.pos = pos;
            this.markedToRemove = false;
            if (!randomColor.getValue()) {
                this.color = colourSetting.getColor().getRGB();
            } else {
                this.color = Color.getHSBColor(rand.nextFloat(), 1.0F, 0.9F).getRGB();
            }
        }

        public void Update() {
            this.pos = this.pos.add(0.0, this.yIncrease, 0.0);
            if (this.timer.passedMs(1000)) {
                this.markedToRemove = true;
            }
        }

        public boolean isMarked() {
            return this.markedToRemove;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        public int getColor() {
            return this.color;
        }
    }
}