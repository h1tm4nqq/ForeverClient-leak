package we.devs.forever.client.modules.impl.combat;

import net.minecraft.block.BlockAir;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.util.TessellatorUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class Aura extends Module {
    public static Aura INSTANCE;
    private final Setting<Float> hitRange = (new Setting<>("Range", 4.3f, 1f, 6f, "How far will aura hits the enemy"));
    private final Setting<Settings> setting = (new Setting<>("Settings", Settings.Targeting));
    //TARGETING
    private final Setting<Boolean> animalSetting = (new Setting<>("Animals", false, "If animals are next to you aura hits them", v -> setting.getValue() == Settings.Targeting));
    private final Setting<Boolean> mobSetting = (new Setting<>("Mobs", true, "If mobs are next to you aura hits them", v -> setting.getValue() == Settings.Targeting));
    private final Setting<Boolean> bullets = (new Setting<>("Bullets", false, "If bullets are next to you aura hits them", v -> setting.getValue() == Settings.Targeting));
    private final Setting<Boolean> playerSetting = (new Setting<>("Players", true, "If players are next to you aura hits them", v -> setting.getValue() == Settings.Targeting));
    private final Setting<Boolean> friendsSetting = (new Setting<>("AttackFriends", false, "If friends are next to you aura hits them", v -> setting.getValue() == Settings.Targeting));
    //ANTICHEAT
    private final Setting<TimingMode> timingMode = (new Setting<>("Timing", TimingMode.Sequential, "Aura will hit in timing", v -> setting.getValue() == Settings.AntiCheat));
    private final Setting<RotationMode> rotations = (new Setting<>("Rotate", RotationMode.Track, "Rotates your camera to entity you hit", v -> setting.getValue() == Settings.AntiCheat));
    private final Setting<Float> wallsRange = (new Setting<>("WallsRange", 3.0f, 0.5f, 6.0f, "", v -> setting.getValue() == Settings.AntiCheat));
    private final Setting<Boolean> strict = (new Setting<>("Strict", false, v -> setting.getValue() == Settings.AntiCheat));
    private final Setting<Float> torqueFactor = (new Setting<>("YawAngle", 1f, 0.1f, 1f, v -> setting.getValue() == Settings.AntiCheat));
    private final Setting<TpsSyncMode> tpsSyncMode = (new Setting<>("TPSSync", TpsSyncMode.Normal, v -> setting.getValue() == Settings.AntiCheat));
    //SPEED
    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Dynamic, v -> setting.getValue() == Settings.Speed));
    private final Setting<Integer> tickDelay = (new Setting<>("TickDelay", 12, 0, 40, v -> setting.getValue() == Settings.Speed && mode.getValue() == Mode.Static));
    //MISC
    private final Setting<Boolean> autoSwitch = (new Setting<>("AutoSwitch", true, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> switchBack = (new Setting<>("SwitchBack", false, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> noGapSwitch = (new Setting<>("NoGapSwitch", true, v -> setting.getValue() == Settings.Misc));
    private final Setting<Swing> swing = (new Setting<>("SwingAnim", Swing.Mainhand, v -> setting.getValue() == Settings.Misc));
    private final Setting<AutoSwitch> switchh = (new Setting<>("SilentKillAura", AutoSwitch.None, v -> setting.getValue() == Settings.Misc));
    private final Setting<Float> attackSwitchDelay = (new Setting<>("AttackSwitchDelay", 0F,0F,5F, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> autoBlock = (new Setting<>("AutoBlock", false, v -> setting.getValue() == Settings.Misc));
    private final Setting<SwordMode> weponMode = (new Setting<>("WeaponMode", SwordMode.SwordAxe, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> onlyInHoles = (new Setting<>("OnlyInHoles", false, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> onlyWhenFalling = (new Setting<>("OnlyWhenFalling", false, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> onlyInAir = (new Setting<>("OnlyInAir", false, v -> setting.getValue() == Settings.Misc));
    private final Setting<Boolean> disableWhenCA = (new Setting<>("DisableWhenCA", false, v -> setting.getValue() == Settings.Misc));
    //private Setting<Boolean> onlyWhenNoTargets = (new Setting<>("OnlyWhenNoTargets", true).withVisibility(disableWhenCA::getValue).withParent(misc);
    //private Setting<Boolean> check32k = (new Setting<>("Check32k", false).withVisibility(() -> swordOnly.getValue()).withParent(misc);
    private final Setting<Boolean> onlyWhenNoTargets = (new Setting<>("OnlyWhenNoTargets", true, v -> setting.getValue() == Settings.Misc && disableWhenCA.getValue()));
    private final Setting<Boolean> check32k = (new Setting<>("Check32k", false, v -> setting.getValue() == Settings.Misc && (weponMode.getValue() == SwordMode.Sword || weponMode.getValue() == SwordMode.SwordAxe)));
    //RENDER
    private final Setting<RenderMode> render = (new Setting<>("RenderMode", RenderMode.Jello, v -> setting.getValue() == Settings.Render));
    private final Setting<Color> circleColor = (new Setting<>("Color", new Color(218, 100, 100, 255), ColorPickerButton.Mode.Normal, 100, v -> setting.getValue() == Settings.Render && !render.getValue().equals(RenderMode.Off)));
    private final Setting<Boolean> onlyWhenHitting = (new Setting<>("OnlyWhenHitting", true, v -> setting.getValue() == Settings.Render && !render.getValue().equals(RenderMode.Off) ));
    private final Setting<Float> animationSpeed = (new Setting<>("AnimSpeed", 1.0F, 0.1F, 10.0F, v -> setting.getValue() == Settings.Render && !render.getValue().equals(RenderMode.Off) ));
    private final Setting<Boolean> depth = (new Setting<>("Depth", true, v -> setting.getValue() == Settings.Render && !render.getValue().equals(RenderMode.Off) ));
    private final Setting<Boolean> fill = (new Setting<>("Fill", false, v -> setting.getValue() == Settings.Render && render.getValue().equals(RenderMode.Orbit) ));
    private final Setting<Boolean> orbit = (new Setting<>("Orbit", true, v -> setting.getValue() == Settings.Render && render.getValue().equals(RenderMode.Orbit) ) );
    private final Setting<Boolean> trial = (new Setting<>("Trail", true, v -> setting.getValue() == Settings.Render && render.getValue().equals(RenderMode.Orbit) ));
    private final Setting<Float> orbitSpeed = (new Setting<>("OrbitSpeed", 1.0F, 0.1F, 10.0F, v -> setting.getValue() == Settings.Render && render.getValue().equals(RenderMode.Orbit) ));
    private final Setting<Float> circleWidth = (new Setting<>("Width", 2.5F, 0.1F, 5F, v -> setting.getValue() == Settings.Render && render.getValue().equals(RenderMode.Orbit) ));

    private enum RotationMode {
        None, Track, Hit
    }

    private enum TimingMode {
        Sequential, Vanilla
    }

    private enum Settings {
        Targeting, AntiCheat, Speed, Misc, Render
    }

    private enum Mode {
        Dynamic, Static
    }

    private enum TpsSyncMode {
        None, Normal, Min, Latest
    }
    public enum  SwordMode {
        None,
        Sword,
        Axe,
        SwordAxe;
    }

    private int ticksRun = 0;

    private static double yaw;
    private static double pitch;

    private long startTime = 0L;

    private int switchBackSlot = -1;

    Entity currentTarget = null;
    SwitchUtil switchUtil =  new SwitchUtil(switchh);

    private TimerUtil lastHit = new TimerUtil();
    private TimerUtil switchTimer = new TimerUtil();

    public Aura() {
        super("Aura", "Aura for... arrow give a fuck", Category.COMBAT);
        INSTANCE = this;
    }
    @Override
    public void onEnable() {
        currentTarget = null;
        switchBackSlot = -1;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if(currentTarget == null ||  !(!onlyWhenHitting.getValue() || !lastHit.passedMs(3500))) return;
        if (render.getValue().equals(RenderMode.Orbit) ) {
            GlStateManager.pushMatrix();
            TessellatorUtil.prepare();
            if (depth.getValue()) {
                GlStateManager.enableDepth();
            }
            float[] hsb = Color.RGBtoHSB(circleColor.getColor().getRed(), circleColor.getColor().getGreen(), circleColor.getColor().getBlue(), null);
            float initialHue = (float) (System.currentTimeMillis() % 7200L) / 7200F;
            float hue = initialHue;
            int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            ArrayList<Vec3d> vecs = new ArrayList<>();
            double x = currentTarget.lastTickPosX + (currentTarget.posX - currentTarget.lastTickPosX) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosX;
            double y = currentTarget.lastTickPosY + (currentTarget.posY - currentTarget.lastTickPosY) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosY;
            double z = currentTarget.lastTickPosZ + (currentTarget.posZ - currentTarget.lastTickPosZ) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosZ;
            double height = -Math.cos(((System.currentTimeMillis() - startTime) / 1000D) * animationSpeed.getValue()) * (currentTarget.height / 2D) + (currentTarget.height / 2D);
            GL11.glLineWidth(circleWidth.getValue());
            GL11.glBegin(1);
            for (int i = 0; i <= 360; ++i) {
                Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * 0.5D, y + height + 0.01D, z + Math.cos((double) i * Math.PI / 180.0) * 0.5D);
                vecs.add(vec);
            }
            for (int j = 0; j < vecs.size() - 1; ++j) {
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;
                float alpha = orbit.getValue() ?
                        trial.getValue() ? (float) Math.max(0, -(1/Math.PI) * Math.atan(Math.tan((Math.PI * (j+1F) / (float) vecs.size() + (System.currentTimeMillis() / 1000D * orbitSpeed.getValue()))))) :
                                (float) Math.max(0, Math.abs(Math.sin((j+1F)/ (float) vecs.size() * Math.PI + (System.currentTimeMillis() / 1000D * orbitSpeed.getValue()))) * 2 - 1) :
                        fill.getValue() ? 1F : circleColor.getValue().getAlpha() / 255F;
                if (circleColor.getRainbow()) {
                    GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha);
                } else {
                    GL11.glColor4f(circleColor.getValue().getRed() / 255F, circleColor.getValue().getGreen() / 255F, circleColor.getValue().getBlue() / 255F, alpha);
                }
                GL11.glVertex3d(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z);
                GL11.glVertex3d(vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z);
                hue += (1F / 360F);
                rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            }
            GL11.glEnd();
            if (fill.getValue()) {
                hue = initialHue;
                GL11.glBegin(GL11.GL_POLYGON);
                for (int j = 0; j < vecs.size() - 1; ++j) {
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = (rgb) & 0xFF;
                    if (circleColor.getRainbow()) {
                        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, circleColor.getValue().getAlpha() / 255F);
                    } else {
                        GL11.glColor4f(circleColor.getValue().getRed() / 255F, circleColor.getValue().getGreen() / 255F, circleColor.getValue().getBlue() / 255F, circleColor.getValue().getAlpha() / 255F);
                    }
                    GL11.glVertex3d(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z);
                    GL11.glVertex3d(vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z);
                    hue += (1F / 360F);
                    rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
                }
                GL11.glEnd();
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            TessellatorUtil.release();
            GlStateManager.popMatrix();
        }
        if (render.getValue().equals(RenderMode.Jello) ) {
            double everyTime = 500 * animationSpeed.getValue();
            double drawTime = (System.currentTimeMillis() % everyTime);
            boolean drawMode = drawTime > (everyTime / 2);
            double drawPercent = drawTime / (everyTime / 2);

            if (!drawMode) {
                drawPercent = 1 - drawPercent;
            } else {
                drawPercent -= 1;
            }

            drawPercent = easeInOutQuad(drawPercent);

            mc.entityRenderer.disableLightmap();
            glPushMatrix();
            glDisable(GL_TEXTURE_2D);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_LINE_SMOOTH);
            glEnable(GL_BLEND);

            if (depth.getValue()) {
                GlStateManager.enableDepth();
            } else glDisable(GL_DEPTH_TEST);

            glDisable(GL_CULL_FACE);
            glShadeModel(7425);
            mc.entityRenderer.disableLightmap();

            double radius = currentTarget.width;
            double height = currentTarget.height + 0.1;

            double x = currentTarget.lastTickPosX + (currentTarget.posX - currentTarget.lastTickPosX) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosX;
            double y = (currentTarget.lastTickPosY + (currentTarget.posY - currentTarget.lastTickPosY) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosY) + height * drawPercent;
            double z = currentTarget.lastTickPosZ + (currentTarget.posZ - currentTarget.lastTickPosZ) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosZ;
            double eased = (height / 3) * ((drawPercent > 0.5) ? 1 - drawPercent : drawPercent) * ((drawMode) ? -1 : 1);

            for (int segments = 0; segments < 360; segments += 5) {
                Color color = circleColor.getColor();

                double x1 = x - Math.sin(segments * Math.PI / 180F) * radius;
                double z1 = z + Math.cos(segments * Math.PI / 180F) * radius;
                double x2 = x - Math.sin((segments - 5) * Math.PI / 180F) * radius;
                double z2 = z + Math.cos((segments - 5) * Math.PI / 180F) * radius;

                glBegin(GL_QUADS);

                glColor4f(pulseColor(color, 200, 1).getRed() / 255.0f, pulseColor(color, 200, 1).getGreen() / 255.0f, pulseColor(color, 200, 1).getBlue() / 255.0f, 0.0f);
                glVertex3d(x1, y + eased, z1);
                glVertex3d(x2, y + eased, z2);

                glColor4f(pulseColor(color, 200, 1).getRed() / 255.0f, pulseColor(color, 200, 1).getGreen() / 255.0f, pulseColor(color, 200, 1).getBlue() / 255.0f, 200.0f);

                glVertex3d(x2, y, z2);
                glVertex3d(x1, y, z1);
                glEnd();

                glBegin(GL_LINE_LOOP);
                glVertex3d(x2, y, z2);
                glVertex3d(x1, y, z1);
                glEnd();
            }

            glEnable(GL_CULL_FACE);
            glShadeModel(7424);
            glColor4f(1f, 1f, 1f, 1f);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_LINE_SMOOTH);
            glDisable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    }
    private double easeInOutQuad(double x) {
        return (x < 0.5) ? 2 * x * x : 1 - Math.pow((-2 * x + 2), 2) / 2;
    }
    public static Color pulseColor(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((System.currentTimeMillis() % ((long)1230675006 ^ 0x495A9BEEL) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + index / (float)count * Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }
    private boolean handlePre() {
        if (disableWhenCA.getValue()) {
                if (AutoCrystal.getInstance().isEnabled()) {
                    if (onlyWhenNoTargets.getValue()) {
                        if (AutoCrystal.getInstance().currentTarget != null) {
                            currentTarget = null;
                            if (switchBack.getValue() && switchBackSlot != -1) {
                                mc.player.inventory.currentItem = switchBackSlot;
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                                switchBackSlot = -1;
                            }
                            return false;
                        }
                    } else {
                        currentTarget = null;
                        if (switchBack.getValue() && switchBackSlot != -1) {
                            mc.player.inventory.currentItem = switchBackSlot;
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                            switchBackSlot = -1;
                        }
                        return false;
                    }
                }
        }

        if(weponMode.getValue() != SwordMode.None) {
            if(!check()) return false;
            if (check32k.getValue()) {
                if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, mc.player.getHeldItemMainhand()) < 6) {
                    currentTarget = null;
                    if (switchBack.getValue() && switchBackSlot != -1) {
                        mc.player.inventory.currentItem = switchBackSlot;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                        switchBackSlot = -1;
                    }
                    return false;
                }
            }
        }

        if (onlyInHoles.getValue()) {
            BlockPos playerPos = new BlockPos(mc.player);
            if (!BlockUtil.isBedrockHole(playerPos) ||!BlockUtil.isObbyHole(playerPos)) {
                currentTarget = null;
                if (switchBack.getValue() && switchBackSlot != -1) {
                    mc.player.inventory.currentItem = switchBackSlot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                    switchBackSlot = -1;
                }
                return false;
            }
        }

        List<Entity> targetsInRange = mc.world.loadedEntityList.
                stream()
                .filter(e -> isValidTarget(e, hitRange.getValue()))
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .collect(Collectors.toList());

        if (!targetsInRange.isEmpty()) {
            if (currentTarget == null || !currentTarget.equals(targetsInRange.get(0))) {
                startTime = System.currentTimeMillis();
            }
            currentTarget = targetsInRange.get(0);
        } else {
            currentTarget = null;
        }

        if (autoBlock.getValue() && currentTarget != null && !mc.player.isActiveItemStackBlocking()) {
            if (mc.player.getHeldItemOffhand().getItem() instanceof ItemShield) {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
            }
        }
        if(!switchTimer.passedMs(attackSwitchDelay.getValue() * 50F)) {
            return false;
        }
        return true;
    }

    public boolean check() {
        if(!switchh.getValue().equals(AutoSwitch.None)) return true;
        switch (weponMode.getValue()) {
            case Axe:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemSword;
            case Sword:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe;
            case SwordAxe:
                return mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe;
            default:
                return false;
        }
    }

    private void handlePost() {
        if (noGapSwitch.getValue() && mc.player.getActiveItemStack().getItem() instanceof ItemFood) return;

        float ticks = 0F;

        if (tpsSyncMode.getValue() == TpsSyncMode.Normal) {
            ticks = 20.0F - tickRateManager.getTickRate();;
        } else if (tpsSyncMode.getValue() == TpsSyncMode.Min) {
            ticks = 20.0F - tickRateManager.getMinTickRate();
        } else if (tpsSyncMode.getValue() == TpsSyncMode.Latest) {
            ticks = 20.0F - tickRateManager.getLatestTickRate();;
        }

        if ((mode.getValue() == Mode.Static && ticksRun < tickDelay.getValue())) {
            ticksRun++;
        }

        float cooledStr = 1F;

        if (currentTarget != null && currentTarget instanceof EntityShulkerBullet) {
            cooledStr = 0F;
        }

        if ((mode.getValue() == Mode.Dynamic && mc.player.getCooledAttackStrength(tpsSyncMode.getValue() != TpsSyncMode.None ? -ticks : 0.0F) >= cooledStr) || (mode.getValue() == Mode.Static && ticksRun >= tickDelay.getValue())) {
            if (!isValidTarget(currentTarget, hitRange.getValue())) {
                currentTarget = null;
            }
        } else if ((mode.getValue() == Mode.Static && ticksRun < tickDelay.getValue())) {
            ticksRun++;
        }

        if ((!onlyWhenFalling.getValue() || mc.player.motionY < 0) && (!onlyInAir.getValue() || mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockAir) && ((mode.getValue() == Mode.Dynamic && mc.player.getCooledAttackStrength(tpsSyncMode.getValue() != TpsSyncMode.None ? -ticks : 0.0F) >= cooledStr) || (mode.getValue() == Mode.Static && ticksRun >= tickDelay.getValue()))) {
            if (currentTarget != null) {
                if (autoSwitch.getValue()) {
                    setSwordSlot(getWeapon());
                }

                boolean sneaking = mc.player.isSneaking();
                boolean sprinting = strict.getValue() && mc.player.isSprinting();
                boolean blocking = mc.player.isActiveItemStackBlocking();

                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                    mc.player.setSprinting(false);
                }

                if (blocking) {
                    if (mc.player.getHeldItemOffhand().getItem() instanceof ItemShield) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(mc.player), EnumFacing.getFacingFromVector((float)((int) mc.player.posX), (float)((int) mc.player.posY), (float)((int) mc.player.posZ))));
                    }
                }
                if(switchh.getValue().equals(AutoSwitch.None)) {
                    mc.playerController.attackEntity(mc.player, currentTarget);
                } else {
                    switchUtil.switchTo(getWeapon());
                    mc.playerController.attackEntity(mc.player, currentTarget);
                    switchUtil.switchBack();
                }

                EntityUtil.swing(swing.getValue());
                lastHit.reset();
                ticksRun = 0;

                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }

                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                    mc.player.setSprinting(true);
                }

                if (blocking) {
                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && mc.player.getHeldItemOffhand().getItem() instanceof ItemShield) {
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
                    }
                }
            } else if (switchBack.getValue() && switchBackSlot != -1) {
                mc.player.inventory.currentItem = switchBackSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                switchBackSlot = -1;
            }
        }
    }

    @EventListener
    public void onUpdateWalkingPlayerPre(MotionEvent.Pre event) {
        if ( event.isCanceled()  || timingMode.getValue() == TimingMode.Vanilla) return;

        if ( mc.world == null || mc.player == null) {
            return;
        }

        if (!handlePre()) {
            return;
        }

        boolean doPost = true;
        if (rotations.getValue() != RotationMode.None && currentTarget != null) {
            if (rotations.getValue() == RotationMode.Hit) {
                float ticks = 0F;

                if (tpsSyncMode.getValue() == TpsSyncMode.Normal) {
                    ticks = 20.0F - tickRateManager.getTickRate();
                } else if (tpsSyncMode.getValue() == TpsSyncMode.Min) {
                    ticks = 20.0F - tickRateManager.getMinTickRate();
                } else if (tpsSyncMode.getValue() == TpsSyncMode.Latest) {
                    ticks = 20.0F - tickRateManager.getLatestTickRate();
                }

                if ((mode.getValue() == Mode.Static && ticksRun < tickDelay.getValue())) {
                    ticksRun++;
                }

                float cooledStr = 1F;

                if (currentTarget != null && currentTarget instanceof EntityShulkerBullet) {
                    cooledStr = 0F;
                }
                if (lastHit.passedMs(5000) || yaw == 0D || (mode.getValue() == Mode.Dynamic && mc.player.getCooledAttackStrength(tpsSyncMode.getValue() != TpsSyncMode.None ? -ticks : 0.0F) >= cooledStr) || (mode.getValue() == Mode.Static && ticksRun >= tickDelay.getValue())) {
                    handleSpacialRotation(currentTarget);
                }
            } else {
                handleSpacialRotation(currentTarget);
            }

            if (torqueFactor.getValue() < 1F) {
                float yawDiff = (float) MathHelper.wrapDegrees(yaw - ((IEntityPlayerSP) mc.player).getLastReportedYaw());
                if (Math.abs(yawDiff) > 180 * torqueFactor.getValue()) {
                    yaw = ((IEntityPlayerSP) mc.player).getLastReportedYaw() + (yawDiff * ((180 * torqueFactor.getValue()) / Math.abs(yawDiff)));
                    doPost = false;
                }
            }
            rotationManager.doRotation(RotationType.Legit, (float) yaw, (float) pitch,9);
        }

        if (doPost) {
            handlePost();
        }
    }

    @Override
    public void onUpdate() {
        if (timingMode.getValue() == TimingMode.Sequential) return;

        if (mc.player == null || mc.world == null) return;

        if (!handlePre()) {
            currentTarget = null;
            return;
        }

        handlePost();
    }

    @EventListener
    private void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer
                && rotations.getValue() != RotationMode.None && currentTarget != null && timingMode.getValue() == TimingMode.Vanilla) {
            handleSpacialRotation(currentTarget);
            CPacketPlayer packet = event.getPacket();
            if (event.getPacket() instanceof CPacketPlayer.Position) {
                event.cancel();
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(mc.player.posX), packet.getY(mc.player.posY), packet.getZ(mc.player.posZ), (float) yaw, (float) pitch, packet.isOnGround()));
            }
        }
        if(event.getPacket() instanceof CPacketHeldItemChange) {
            switchTimer.reset();
        }
    }

    private boolean isValidTarget(Entity entity, float range) {
        if (entity == mc.player || entity == mc.getRenderViewEntity()) {
            return false;
        }

        if (bullets.getValue() && entity instanceof EntityShulkerBullet && !entity.isDead && rangeCheck(entity, range) && (doRayTrace(entity))) {
            return true;
        }

        if (!(entity instanceof EntityLivingBase)) {
            return false;
        }

        if (!shouldAttack(entity)) {
            return false;
        }

        if (entity.isDead) {
            return false;
        }

        if (((EntityLivingBase) entity).getHealth() <= 0) {
            return false;
        }

        if (!rangeCheck(entity, range)) {
            return false;
        }

        if (!doRayTrace(entity)) {
            return false;
        }

//        if (AntiBot.getBots().contains(entity)) {
//            return false;
//        }

        return true;
    }

    public boolean rangeCheck(Entity e, float range) {
        AxisAlignedBB bb = e.getEntityBoundingBox();

        for (double xS = 0.15D; xS < 0.85D; xS += 0.1D) {
            for (double yS = 0.15D; yS < 0.85D; yS += 0.1D) {
                for (double zS = 0.15D; zS < 0.85D; zS += 0.1D) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    if (mc.player.getDistance(tempVec.x, tempVec.y, tempVec.z) < range) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public  boolean doRayTrace(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();

        for (double xS = 0.15D; xS < 0.85D; xS += 0.1D) {
            for (double yS = 0.15D; yS < 0.85D; yS += 0.1D) {
                for (double zS = 0.15D; zS < 0.85D; zS += 0.1D) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    if (isVisible(tempVec)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public  boolean isVisible(Vec3d vec3d) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox()).minY + mc.player.getEyeHeight(), mc.player.posZ);
        return (mc.world.rayTraceBlocks(eyesPos, vec3d) == null) || vec3d.distanceTo(mc.player.getPositionEyes(1F)) <= wallsRange.getValue();
    }

    public boolean shouldAttack(Entity e) {
        if (animalSetting.getValue() && e instanceof EntityAnimal) {
            return true;
        } else if (mobSetting.getValue() && e instanceof IMob) {
            return true;
        } else if (playerSetting.getValue() && e instanceof EntityPlayer) {
            if(!friendsSetting.getValue()) {
                return !friendManager.isFriend(e.getName());
            } else {
                return true;
            }
        }

        return false;

    }

    public void handleSpacialRotation(Entity e) {
        AxisAlignedBB bb = e.getEntityBoundingBox();
        Vec3d gEyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox()).minY + mc.player.getEyeHeight(), mc.player.posZ);

        Vec3d finalVec = null;
        double[] finalRotation = null;

        for (double xS = 0.15D; xS < 0.85D; xS += 0.1D) {
            for (double yS = 0.15D; yS < 0.85D; yS += 0.1D) {
                for (double zS = 0.15D; zS < 0.85D; zS += 0.1D) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    if (isVisible(tempVec)) {
                        double diffX = tempVec.x - gEyesPos.x;
                        double diffY = tempVec.y - gEyesPos.y;
                        double diffZ = tempVec.z - gEyesPos.z;
                        double[] tempRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))};
                        if (finalVec != null && finalRotation != null) {
                            if (Math.hypot((((tempRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                    Math.hypot((((finalRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (finalRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                finalVec = tempVec;
                                finalRotation = tempRotation;
                            }
                        } else {
                            finalVec = tempVec;
                            finalRotation = tempRotation;
                        }
                    }
                }
            }
        }
        if (finalVec != null && finalRotation != null) {
            double yawDiff = ((finalRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F;
            double pitchDiff = ((finalRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()) % 360.0F + 540.0F) % 360.0F - 180.0F;
            double[] finalYawPitch = new double[]{((IEntityPlayerSP) mc.player).getLastReportedYaw() + ((yawDiff > 180.0F) ? 180.0F : Math.max(yawDiff, -180.0F)), ((IEntityPlayerSP) mc.player).getLastReportedPitch() + ((pitchDiff > 180.0F) ? 180.0F : Math.max(pitchDiff, -180.0F))};
            setYawAndPitch((float) finalYawPitch[0], (float) finalYawPitch[1]);
        }
    }

    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
    }

    public void onDisable() {
        if (mc.player != null) {
            if (switchBack.getValue() && switchBackSlot != -1) {
                mc.player.inventory.currentItem = switchBackSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                switchBackSlot = -1;
            }
        }
    }

    public int getWeapon() {

        int weaponSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) {
            weaponSlot = mc.player.inventory.currentItem;
        }


        if (weaponSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.DIAMOND_SWORD) {
                    weaponSlot = l;
                    break;
                }
            }
        }

        return weaponSlot;

    }

    public void setSwordSlot(int swordSlot) {
        if (mc.player.inventory.currentItem != swordSlot && swordSlot != -1) {
            if (switchBack.getValue()) {
                switchBackSlot = mc.player.inventory.currentItem;
            }
            mc.player.inventory.currentItem = swordSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(swordSlot));
        }
    }
    public enum RenderMode{
        Off,
        Jello,
        Orbit
    }
}