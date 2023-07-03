package we.devs.forever.client.modules.impl.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.images.Image;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.api.util.shaders.impl.fill.*;
import we.devs.forever.api.util.shaders.impl.outline.*;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.modules.impl.render.BreakHighlight;
import we.devs.forever.client.modules.impl.render.BurrowHighlight;
import we.devs.forever.client.modules.impl.render.PopChams;
import we.devs.forever.client.modules.impl.render.breadcrumbs.BreadCrumbs;
import we.devs.forever.client.modules.impl.render.holeesp.HoleESP;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;
import we.devs.forever.client.ui.newGui.api.Beta;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Beta("NotWork")
public class Shader extends Module {
    public static Shader INSTANCE;
    public static Setting<Page> page = new Setting<>("Page", Page.Render);

    public static Setting<fillShadermode> fillShader = (new Setting<>("Fill Shader", fillShadermode.None, v -> page.getValue().equals(Page.Render)));
    public static Setting<Color> fillColor = (new Setting<>("FillColor", new Color(2, 101, 250, 255), ColorPickerButton.Mode.Normal, 100, v -> page.getValue().equals(Page.Render)));
    public Setting<Image> image = (new Setting<>("Image", new Image("NONE"), v -> page.getValue().equals(Page.Render) && fillShader.getValue().equals(fillShadermode.Image)));
    public Setting<Integer> alpha = (new Setting<>("ImageAlpha", 255, 0, 255, v -> page.getValue().equals(Page.Render) && fillShader.getValue().equals(fillShadermode.Image)));
    public static Setting<Float> speedFill = (new Setting<>("Speed Fill", 10.0f, 1.0f, 100.0f, v -> page.getValue().equals(Page.Render)));
    public static Setting<glowESPmode> glowESP = (new Setting<>("OutLine Shader", glowESPmode.None, v -> page.getValue().equals(Page.Render)));
    public static Setting<Color> outlineColor = (new Setting<>("OutlineColor", new Color(2, 101, 250, 255), ColorPickerButton.Mode.Normal, 100, v -> page.getValue().equals(Page.Render)));
    public static Setting<Float> quality = (new Setting<>("quality", 1.0f, 0.0f, 20.0f, v -> page.getValue().equals(Page.Render)));
    public static Setting<Float> radius = (new Setting<>("radius", 1.0f, 0.0f, 5.0f, v -> page.getValue().equals(Page.Render)));
    public static Setting<Boolean> fade = (new Setting<>("Fade", false, v -> page.getValue().equals(Page.Render)));
    public static Setting<Float> speedOutline = (new Setting<>("Speed Outline", 10.0f, 1.0f, 100.0f, v -> page.getValue().equals(Page.Render)));
    public static Setting<Mode> items = (new Setting<>("Items", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Boolean> itemsCancel = (new Setting<>("ItemsCancel", false, v -> page.getValue().equals(Page.Entity) && !items.getValue().equals(Mode.None)));
    public static Setting<Mode> itemsFill = (new Setting<>("DroppedItems", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> mobsFill = (new Setting<>("Mobs", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> playersFill = (new Setting<>("Players", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> crystalsFill = (new Setting<>("Crystals", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> xpFill = (new Setting<>("XP", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> bottleFill = (new Setting<>("Bottle", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> boatFill = (new Setting<>("Boat", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> minecartFill = (new Setting<>("MinecartTnt", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> enderPerleFill = (new Setting<>("EnderPerle", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Mode> arrowFill = (new Setting<>("Arrow", Mode.None, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Boolean> rangeCheck = (new Setting<>("Range Check", true, v -> page.getValue().equals(Page.Entity)));
    public static Setting<Float> minRange = (new Setting<>("Min range", 1F, 0F, 5F, v -> rangeCheck.getValue() && page.getValue().equals(Page.Entity)));
    public static Setting<Float> maxRange = (new Setting<>("Max Range", 20F, 10F, 100F, v -> rangeCheck.getValue() && page.getValue().equals(Page.Entity)));

    public static Setting<Mode> popChams = (new Setting<>("PopChams", Mode.None, v -> page.getValue().equals(Page.Modules)));
    public static Setting<Mode> holeEsp = (new Setting<>("HoleEsp", Mode.None, v -> page.getValue().equals(Page.Modules)));
    public static Setting<Mode> crystalka = (new Setting<>("AutoCrystal", Mode.None, v -> page.getValue().equals(Page.Modules)));
    public static Setting<Mode> burrowHighLight = (new Setting<>("BurrowHighLight", Mode.None, v -> page.getValue().equals(Page.Modules)));
    public static Setting<Mode> breakHighLight = (new Setting<>("BreakHighLight", Mode.None, v -> page.getValue().equals(Page.Modules)));
    public static Setting<Mode> breadCrumbs = (new Setting<>("BreadCrumbs", Mode.None, v -> page.getValue().equals(Page.Modules)));
    //   public static Setting<Integer> maxEntities = (new Setting<>("Max Entities", 100, 10, 500));
    List<Runnable> entitiesFill = new CopyOnWriteArrayList<>();
    List<Runnable> entitiesOutLine = new CopyOnWriteArrayList<>();
    ModuleThread<Shader> chamsModuleThread = new RenderThread(this);
    public volatile boolean crit = false;

    public Shader() {
        super("Shader", "Shader", Module.Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        chamsModuleThread.start();
    }

    @Override
    public void onDisable() {
        chamsModuleThread.getThread().interrupt();
    }

    @Override
    public void onLoad() throws Throwable {
        if (isEnabled()) chamsModuleThread.start();
    }

    @Override
    public void onUnload() throws Throwable {
        chamsModuleThread.getThread().interrupt();
    }

    @Override
    public void onAltRender3D(float ticks) {
        if (mc.world == null || mc.player == null || (entitiesFill.isEmpty() && entitiesOutLine.isEmpty())) return;

        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        if (fillShader.getValue() != fillShadermode.None && !entitiesFill.isEmpty()) {
            runPreFill();
            entitiesFill.stream().filter(Objects::nonNull).forEach(Runnable::run);
        }
        if (glowESP.getValue() != glowESPmode.None && !entitiesOutLine.isEmpty()) {
            runPreGlow();
            entitiesOutLine.stream().filter(Objects::nonNull).forEach(Runnable::run);
        }
        if (fillShader.getValue() != fillShadermode.None && !entitiesFill.isEmpty()) {
            runPostFill();
        }

        if (glowESP.getValue() != glowESPmode.None && !entitiesOutLine.isEmpty()) {
            runPostGlow();
        }
        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
    }


    boolean renderPlayersFill(Entity e) {
        try {
            if(!isInFov(e.getPosition())) return false;

            if (checkFill(e)) {
                double distancePl = mc.player.getDistanceSq(e);
                if (!rangeCheck.getValue() || (distancePl > minRange.getValue() * minRange.getValue() && distancePl < maxRange.getValue() * maxRange.getValue())) {
                    return true;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;

    }
    public boolean isInFov(BlockPos pos) {
        int yaw = getYaw4D();

        if (yaw == 0 && (double) pos.getZ() - mc.player.getPositionVector().z < 0.0) {
            return false;
        }
        if (yaw == 1 && (double) pos.getX() - mc.player.getPositionVector().x > 0.0) {
            return false;
        }
        if (yaw == 2 && (double) pos.getZ() - mc.player.getPositionVector().z > 0.0) {
            return false;
        }

        return yaw != 3 || (double) pos.getX() - mc.player.getPositionVector().x >= 0.0;
    }
    public int getYaw4D() {
        return MathHelper.floor((double) (mc.player.rotationYaw * 4.0f / 360.0f) + 0.5) & 3;
    }
    boolean renderPlayersOutLine(Entity e) {
        try {
            if(!isInFov(e.getPosition())) return false;
            if (checkOutLine(e)) {
                double distancePl = mc.player.getDistanceSq(e);
                if (!rangeCheck.getValue()
                        || (distancePl > minRange.getValue() * minRange.getValue() && distancePl < maxRange.getValue() * maxRange.getValue())) {
                    return true;
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;

    }

    public Vec3d getInterpolatedRenderPos(final Entity entity, final float ticks) {
        return interpolateEntity(entity, ticks).subtract(Minecraft.getMinecraft().getRenderManager().viewerPosX, Minecraft.getMinecraft().getRenderManager().viewerPosY, Minecraft.getMinecraft().getRenderManager().viewerPosZ);
    }

    public static Vec3d interpolateEntity(Entity entity, float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time);
    }

    public boolean checkFill(Object e) {
        if (e instanceof EntityPlayer) {
            if (playersFill.getValue().equals(Mode.Fill) || playersFill.getValue().equals(Mode.Both))
                return e != mc.player || mc.gameSettings.thirdPersonView != 0;
        } else if (e instanceof EntityItem) {
            return itemsFill.getValue().equals(Mode.Fill) || itemsFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityCreature) {
            return mobsFill.getValue().equals(Mode.Fill) || mobsFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityEnderCrystal) {
            return crystalsFill.getValue().equals(Mode.Fill) || crystalsFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityXPOrb) {
            return xpFill.getValue().equals(Mode.Fill) || xpFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityExpBottle) {
            return bottleFill.getValue().equals(Mode.Fill) || bottleFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityBoat) {
            return boatFill.getValue().equals(Mode.Fill) || boatFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityMinecart) {
            return minecartFill.getValue().equals(Mode.Fill) || minecartFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityEnderPearl) {
            return enderPerleFill.getValue().equals(Mode.Fill) || enderPerleFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityArrow) {
            return arrowFill.getValue().equals(Mode.Fill) || arrowFill.getValue().equals(Mode.Both);
        } else if (e instanceof HoleESP) {
            return HoleESP.esp.isEnabled() && (holeEsp.getValue().equals(Mode.Fill) || holeEsp.getValue().equals(Mode.Both));
        } else if (e instanceof PopChams) {
            return PopChams.popChams.isEnabled() && (popChams.getValue().equals(Mode.Fill) || popChams.getValue().equals(Mode.Both));
        } else if (e instanceof AutoCrystal) {
            return AutoCrystal.INSTANCE.isEnabled() && (crystalka.getValue().equals(Mode.Fill) || crystalka.getValue().equals(Mode.Both));
        } else if (e instanceof BurrowHighlight) {
            return BurrowHighlight.burrowHighlight.isEnabled() && (burrowHighLight.getValue().equals(Mode.Fill) || burrowHighLight.getValue().equals(Mode.Both));
        } else if (e instanceof BreakHighlight) {
            return BreakHighlight.breakHighlight.isEnabled() && (breakHighLight.getValue().equals(Mode.Fill) || breakHighLight.getValue().equals(Mode.Both));
        } else if (e instanceof BreadCrumbs) {
            return BreadCrumbs.breadCrumbs.isEnabled() && (breadCrumbs.getValue().equals(Mode.Fill) || breadCrumbs.getValue().equals(Mode.Both));
        }
        return false;
    }

    public boolean checkOutLine(Object e) {
        if (e instanceof EntityPlayer) {
            if (playersFill.getValue().equals(Mode.OutLine) || playersFill.getValue().equals(Mode.Both))
                return e != mc.player || mc.gameSettings.thirdPersonView != 0;
        } else if (e instanceof EntityItem) {
            return itemsFill.getValue().equals(Mode.OutLine) || itemsFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityCreature) {
            return mobsFill.getValue().equals(Mode.OutLine) || mobsFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityEnderCrystal) {
            return crystalsFill.getValue().equals(Mode.OutLine) || crystalsFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityXPOrb) {
            return xpFill.getValue().equals(Mode.OutLine) || xpFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityExpBottle) {
            return bottleFill.getValue().equals(Mode.OutLine) || bottleFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityBoat) {
            return boatFill.getValue().equals(Mode.OutLine) || boatFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityMinecart) {
            return minecartFill.getValue().equals(Mode.OutLine) || minecartFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityEnderPearl) {
            return enderPerleFill.getValue().equals(Mode.OutLine) || enderPerleFill.getValue().equals(Mode.Both);
        } else if (e instanceof EntityArrow) {
            return arrowFill.getValue().equals(Mode.OutLine) || arrowFill.getValue().equals(Mode.Both);
        } else if (e instanceof HoleESP) {
            return HoleESP.esp.isEnabled() && (holeEsp.getValue().equals(Mode.OutLine) || holeEsp.getValue().equals(Mode.Both));
        } else if (e instanceof PopChams) {
            return PopChams.popChams.isEnabled() && (popChams.getValue().equals(Mode.OutLine) || popChams.getValue().equals(Mode.Both));
        } else if (e instanceof AutoCrystal) {
            return AutoCrystal.INSTANCE.isEnabled() && (crystalka.getValue().equals(Mode.OutLine) || crystalka.getValue().equals(Mode.Both));
        } else if (e instanceof BurrowHighlight) {
            return BurrowHighlight.burrowHighlight.isEnabled() && (burrowHighLight.getValue().equals(Mode.OutLine) || burrowHighLight.getValue().equals(Mode.Both));
        } else if (e instanceof BreakHighlight) {
            return BreakHighlight.breakHighlight.isEnabled() && (breakHighLight.getValue().equals(Mode.OutLine) || breakHighLight.getValue().equals(Mode.Both));
        } else if (e instanceof BreadCrumbs) {
            return BreadCrumbs.breadCrumbs.isEnabled() && (breadCrumbs.getValue().equals(Mode.OutLine) || breadCrumbs.getValue().equals(Mode.Both));
        }

        return false;
    }

    public void runPreGlow() {
        float ticks = mc.getRenderPartialTicks();
        switch (glowESP.getValue()) {
            case Color: {
                GlowShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Gradient: {
                GradientOutlineShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Astral: {
                AstralOutlineShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Aqua: {
                AquaOutlineShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Circle: {
                CircleOutlineShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Rainbow: {
                RainbowOutLineShader.INSTANCE.startDraw(ticks);
                break;
            }
        }
    }
    public void runPostGlow() {
        switch (glowESP.getValue()) {
            case Color: {
                GlowShader.INSTANCE.stopDraw(outlineColor.getColor(), radius.getValue(), quality.getValue(), fade.getValue());
                break;
            }
            case Gradient: {
                GradientOutlineShader.INSTANCE.stopDraw(outlineColor.getColor(), radius.getValue(), quality.getValue(), fade.getValue(), 1, 1F, 1, 1, 255F, 1);
                GradientOutlineShader.INSTANCE.update(speedOutline.getValue() / 1000.0f);
                break;
            }
            case Astral: {
                AstralOutlineShader.INSTANCE.stopDraw(outlineColor.getColor(), radius.getValue(), quality.getValue(), fade.getValue(), 255, 1.0F, 6, 1.0F, 20F, 5.4F, 0.2F, 0.9F, 1F, 0.8F, 1);
                AstralOutlineShader.INSTANCE.update(speedOutline.getValue() / 1000.0f);
                break;
            }
            case Aqua: {
                AquaOutlineShader.INSTANCE.stopDraw(outlineColor.getColor(), radius.getValue(), quality.getValue(), fade.getValue(), 2.0F, 7, 14.4F);
                AquaOutlineShader.INSTANCE.update(speedOutline.getValue() / 1000.0f);
                break;
            }
            case Circle: {
                CircleOutlineShader.INSTANCE.stopDraw(outlineColor.getColor(), radius.getValue(), quality.getValue(), fade.getValue(), 225, 1F, 3.1415926535897932384626433832795028841971F, 1F);
                CircleOutlineShader.INSTANCE.update(speedOutline.getValue() / 1000.0f);
                break;
            }
            case Rainbow: {
                RainbowOutLineShader.INSTANCE.stopDraw(outlineColor.getColor(), radius.getValue(), quality.getValue(), fade.getValue());
                RainbowOutLineShader.INSTANCE.update(speedOutline.getValue() / 1000.0f);
                break;
            }
        }

    }

    public void runPreFill() {
        float ticks = mc.getRenderPartialTicks();
        switch (fillShader.getValue()) {
            case Astral: {
                FlowShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Aqua: {
                AquaShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Gradient: {
                GradientShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Fill: {
                FillShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Wave: {
                WaveShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Phobos: {
                PhobosShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Image: {
                ImageShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Rainbow: {
                RainbowShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Octagrams: {
                OctagrasmsShader.INSTANCE.startDraw(ticks);
                break;
            }
            case Nebula: {
                NebulaShader.INSTANCE.startDraw(ticks);
                break;
            }
        }
    }



    public void runPostFill() {
        switch (fillShader.getValue()) {
            case Astral: {
                FlowShader.INSTANCE.stopDraw(fillColor.getColor(), 1.0f, 1.0f, 1.0F, 6, 1.0F, 20F, 5.4F, 0.2F, 0.9F, 1F, 0.8F, 0);
                FlowShader.INSTANCE.update(speedFill.getValue() / 1000.0f);
                break;
            }
            case Aqua: {
                AquaShader.INSTANCE.stopDraw(fillColor.getColor(), 1.0f, 1.0f, 1.0F, 7, 14.4F);
                AquaShader.INSTANCE.update(speedFill.getValue() / 1000.0f);
                break;
            }
            case Gradient: {
                GradientShader.INSTANCE.stopDraw(fillColor.getColor(), 1.0f, 1.0f, 1.0F, 1F, 1F, 255, 1);
                GradientShader.INSTANCE.update(speedFill.getValue() / 1000.0f);
                break;
            }
            case Fill: {
                FillShader.INSTANCE.stopDraw(fillColor.getColor());
                FillShader.INSTANCE.update(speedFill.getValue() / 1000.0f);
                break;
            }
            case Phobos: {
                PhobosShader.INSTANCE.stopDraw(fillColor.getColor(), 1.0f, 1.0f, 1.0F, 1, 1F);
                PhobosShader.INSTANCE.update(speedFill.getValue() / 1000.0f);
                break;
            }
            case Image: {
                ImageShader.INSTANCE.stopDraw(fillColor.getColor(), false, alpha.getValue(), true, 255, image.getValue());
                break;
            }
            case Rainbow: {
                RainbowShader.INSTANCE.stopDraw(fillColor.getColor());
                RainbowShader.INSTANCE.update(speedFill.getValue() / 10000.0f);
                break;
            }
            case Wave: {
                WaveShader.INSTANCE.stopDraw(fillColor.getColor());
                WaveShader.INSTANCE.update(speedFill.getValue() / 10000.0f);
                break;
            }
            case Octagrams: {
                OctagrasmsShader.INSTANCE.stopDraw(fillColor.getColor());
                OctagrasmsShader.INSTANCE.update(speedFill.getValue() / 10000.0f);
                break;
            }
            case Nebula: {
                NebulaShader.INSTANCE.stopDraw(fillColor.getColor());
                NebulaShader.INSTANCE.update(speedFill.getValue() / 10000.0f);
                break;
            }
        }

    }


    public enum fillShadermode {
        None,
        Fill,
        Image,
        Gradient,
        Rainbow,
        Wave,
        Octagrams,
        Nebula,
        Phobos,
        Astral,
        Aqua,


    }

    public enum glowESPmode {
        None,
        Color,
        Gradient,
        Rainbow,
        Circle,
        Astral,
        Aqua
    }

    public enum Mode {
        None,
        Fill,
        OutLine,
        Both
    }

    public enum Page {
        Render,
        Entity,
        Modules
    }
}
